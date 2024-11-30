package com.wiserate.services;

import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import com.wiserate.helpers.LandTransferTax;
import com.wiserate.helpers.LoanAmortization;
import com.wiserate.helpers.LoanHelpers;
import com.wiserate.models.AmortizationPayment;
import com.wiserate.models.CalculatedAmounts;
import com.wiserate.models.Fees;
import com.wiserate.models.Loan;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

@Slf4j
@Service
public class LoanCalculatorService {

    // Using Lombok @Slf4j annotation is equivalent to the following and
    // is preferred as it generates the logger at compile time:
    // private static final Logger log = LoggerFactory.getLogger(LoanCalculatorService.class);

    private final LandTransferTax landTransferTax;
    private final LoanHelpers loanHelpers;
    private final LoanAmortization loanAmortization;


    public LoanCalculatorService(LandTransferTax landTransferTax, LoanHelpers loanHelpers, LoanAmortization loanAmortization) {
        this.landTransferTax = landTransferTax;
        this.loanHelpers = loanHelpers;
        this.loanAmortization = loanAmortization;
    }


    // initialize all data into the Loan object
    public Loan initialize(Loan loan) {
        log.debug("INITIALIZED THE LOAN CALCULATOR SERVICE....");

        if (loan.getFees() == null) loan.setFees(new Fees());
        if (loan.getCalculatedAmounts() == null) loan.setCalculatedAmounts(new CalculatedAmounts());

        // PAYMENTS PER YEAR
        int paymentsPerYear = loanHelpers.noOfPaymentsPerYear(loan.getPaymentFrequency());
        int termToYears = loan.getLoanTermMonths() / 12;

        Fees fees = loan.getFees();
        CalculatedAmounts calculatedAmounts = loan.getCalculatedAmounts();

        // PROPERTY REAL VALUE
        BigDecimal propertyValue = loan.getTotalLoanAmount();

        // Down-Payment
        BigDecimal downPayment = loan.getDownPayment();

        // Principal
        BigDecimal principal = propertyValue.subtract(downPayment);

        // CMHC INSURANCE
        BigDecimal cmhcInsurance = loanHelpers.calculateCMHCPremium(principal, loanHelpers.calculateDownPaymentPercentage(loan.getDownPayment(), propertyValue));
        calculatedAmounts.setCmhcInsurance(cmhcInsurance);

        // SET PRINCIPAL
        // loan.setPrincipal(propertyValue - loan.getDownPayment() + cmhcInsurance);
        loan.setPrincipal(principal.add(cmhcInsurance));

        // SET ANNUAL INTEREST RATE
        // Complete Precision
        loan.setAnnualInterestRate(loan.getAnnualInterestRate().divide(BigDecimal.valueOf(100), MathContext.DECIMAL64));

        // CALCULATE EQUAL PERIODIC PAYMENT
        BigDecimal periodicPayment = calculatePeriodicPayment(loan);

        // SET PERIODIC PAYMENT
        loan.setPeriodicPayment(periodicPayment);

        // TOTAL PAYMENT FOR LOAN TERM
        BigDecimal totalPayment = periodicPayment
                .multiply(BigDecimal.valueOf(termToYears))
                .multiply(BigDecimal.valueOf(paymentsPerYear));

        // TOTAL INTEREST
        loan.setTotalInterest(totalPayment.subtract(loan.getPrincipal()));

        // SET TOTAL PAYMENT
        loan.setTotalPayment(totalPayment);

        // Set Ontario if province is null
        if (loan.getProvince() == null) {
            loan.setProvince(ProvinceCA.ON);
        }

        // SET MUNICIPALITY
        if (loan.getMunicipality() == null) {
            loan.setMunicipality("toronto");
        }

        // LAND TRANSFER TAX
        BigDecimal provincialLandTransferTax = landTransferTax.calculate(propertyValue, loan.getProvince().toString());
        calculatedAmounts.setProvincialLandTransferTax(provincialLandTransferTax);

        // MUNICIPAL LAND TRANSFER TAX [ONLY TORONTO]
        BigDecimal municipalLandTransferTax = BigDecimal.ZERO;
        if (loan.getMunicipality().equalsIgnoreCase("toronto")) {
            municipalLandTransferTax = landTransferTax.calculate(propertyValue, "ON");
        }
        calculatedAmounts.setMunicipalLandTransferTax(municipalLandTransferTax);

        // LAWYER FEE
        if (fees.getLawyerFee().compareTo(BigDecimal.ZERO) == 0) {
            fees.setLawyerFee(BigDecimal.valueOf(1000.0));
        }

        // MAXIMUM TAX REBATE [ FIRST TIME HOME BUYER ]
        BigDecimal maxTaxRebate = BigDecimal.ZERO;
        if (loan.isNewHomeBuyer() && loan.getProvince() == ProvinceCA.ON) {
            maxTaxRebate = loanHelpers.getMaxTaxRebate(loan.getProvince().toString(), loan.getMunicipality());
        } else if (loan.isNewHomeBuyer()) {
            maxTaxRebate = loanHelpers.getMaxTaxRebate(loan.getProvince().toString());
        }

        BigDecimal totalTaxes = provincialLandTransferTax.add(municipalLandTransferTax);
        // Rebate cannot exceed total taxes
        if (maxTaxRebate.compareTo(totalTaxes) > 0) {
            maxTaxRebate = totalTaxes;
        }

        calculatedAmounts.setLandTransferTaxRebate(maxTaxRebate);

        // PROVINCIAL SALES TAX [ PST ]
        BigDecimal pst = loanHelpers.calculatePST(cmhcInsurance, loan.getProvince().toString());
        calculatedAmounts.setProvincialSalesTax(pst);

        BigDecimal finalLandTransferTax = provincialLandTransferTax.add(municipalLandTransferTax).subtract(maxTaxRebate);

        // CASH TO CLOSE
        BigDecimal cashToClose = loan.getDownPayment()
                .add(finalLandTransferTax)
                .add(calculatedAmounts.getProvincialSalesTax())
                .add(fees.getLawyerFee())
                .add(fees.getTitleInsurance())
                .add(fees.getHomeInspectionFee())
                .add(fees.getAppraisalFee())
                .add(fees.getOtherFees())
                .round(MathContext.DECIMAL64);
        ;

        loan.setCashToClose(cashToClose);

        loan.setFees(fees);
        loan.setCalculatedAmounts(calculatedAmounts);


        // START DATE TODAY IF NULL
        if (loan.getStartDate() == null) {
            loan.setStartDate(LocalDate.now());
        }

        // END DATE
        LocalDate endDate = loanHelpers.calculateEndDate(loan.getStartDate(), loan.getLoanTermMonths() / 12);
        loan.setEndDate(endDate);

        log.debug("LOAN CALCULATION COMPLETED....");

        List<AmortizationPayment> payment = loanAmortization.generateAmortizationSchedule(loan);
        loan.setAmortizationSchedule(payment);

        // RETURN LOAN
        return loan;
    }


    public BigDecimal calculatePeriodicPaymentCompoundInterest(
            BigDecimal principal,
            BigDecimal annualRate,
            int compoundingFrequency,
            BigDecimal termInYears,
            int paymentsPerYear) {
        log.debug("CALCULATING PERIODIC PAYMENT FOR COMPOUND INTEREST....");
        log.debug("Principal: {}\tAnnual Rate: {}\tCompounding Frequency: {}\tTerm in Years: {}\tPayments per Year: {}",
                principal, annualRate, compoundingFrequency, termInYears, paymentsPerYear);

        // Validate inputs
        if (principal.compareTo(BigDecimal.ZERO) <= 0 ||
                annualRate.compareTo(BigDecimal.ZERO) <= 0 ||
                compoundingFrequency <= 0 ||
                termInYears.compareTo(BigDecimal.ZERO) <= 0 ||
                paymentsPerYear <= 0) {

            return BigDecimal.ZERO;
        }

        // Calculate the effective interest rate
        BigDecimal one = BigDecimal.ONE;
        BigDecimal compoundingFrequencyBD = BigDecimal.valueOf(compoundingFrequency);
        BigDecimal paymentsPerYearBD = BigDecimal.valueOf(paymentsPerYear);


        try {

            // Calculate the periodic interest rate per payment period
            double r_c = annualRate.doubleValue() / compoundingFrequency;
            double exponent = (double) compoundingFrequency / paymentsPerYear;
            double periodicRate = Math.pow(1 + r_c, exponent) - 1;

            BigDecimal r = BigDecimal.valueOf(periodicRate);


            //  PMT = P * r * (1 + r)^n / (1 + r)^n - 1
            // BigDecimal r = annualRate.divide(compoundingFrequencyBD, MathContext.DECIMAL64);
            BigDecimal n = termInYears.multiply(paymentsPerYearBD);
            BigDecimal cf = one.add(r).pow(n.intValue(), MathContext.DECIMAL64);
            BigDecimal numerator = principal.multiply(r).multiply(cf);
            BigDecimal denominator = cf.subtract(one);

            return numerator.divide(denominator, MathContext.DECIMAL64);

        } catch (ArithmeticException e) {
            log.error("Error in calculating periodic payment: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }


    // Calculate equal amount of payment for full term of loan
    public BigDecimal calculatePeriodicPayment(Loan loan) {
        log.debug("INITIALIZING PERIODIC PAYMENT CALCULATION....");

        BigDecimal principal = loan.getPrincipal();                 // a
        BigDecimal annualRate = loan.getAnnualInterestRate();       // r

        int termInMonths = loan.getLoanTermMonths();
        BigDecimal termInYears = BigDecimal.valueOf(termInMonths / 12.0);       // t

        boolean isCompoundInterest = loan.getIsCompoundInterest();

        // YEARLY PAYMENTS [NUMBER]     n
        int paymentsPerYear = loanHelpers.noOfPaymentsPerYear(loan.getPaymentFrequency());

        log.debug("Payments per year: {}", paymentsPerYear);

        BigDecimal periodicPayment;

        if (isCompoundInterest) {

            int compoundingFrequency = loan.getCompoundFrequency();

            periodicPayment = calculatePeriodicPaymentCompoundInterest(
                    principal,
                    annualRate,
                    compoundingFrequency,
                    termInYears,
                    paymentsPerYear
            );
        } else {
            // double totalInterest = principal * annualRate * termInYears;
            // double totalAmount = principal + totalInterest;
            BigDecimal totalInterest = principal.multiply(annualRate).multiply(termInYears);
            BigDecimal totalAmount = principal.add(totalInterest);
            // Total number of payments
            int totalPayments = (int) (termInMonths / (12.0 / paymentsPerYear));
            // Calculate periodic payment
            // periodicPayment = Math.round(totalAmount / totalPayments * 100.0) / 100.0;
            periodicPayment = totalAmount.divide(BigDecimal.valueOf(totalPayments), 2, RoundingMode.HALF_UP);
        }

        log.debug("PERIODIC PAYMENT CALCULATED: {}", periodicPayment);
        return periodicPayment;
    }


}
