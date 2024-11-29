package com.wiserate.services;

import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import com.wiserate.helpers.LandTransferTax;
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


    public LoanCalculatorService(LandTransferTax landTransferTax, LoanHelpers loanHelpers) {
        this.landTransferTax = landTransferTax;
        this.loanHelpers = loanHelpers;
    }


    // initialize all data into the Loan object
    public Loan initialize(Loan loan) {
        log.debug("INITIALIZED THE LOAN CALCULATOR SERVICE....");

        if (loan.getFees() == null) loan.setFees(new Fees());
        if (loan.getCalculatedAmounts() == null) loan.setCalculatedAmounts(new CalculatedAmounts());

        Fees fees = loan.getFees();
        CalculatedAmounts calculatedAmounts = loan.getCalculatedAmounts();

        // PROPERTY REAL VALUE
        BigDecimal propertyValue = loan.getTotalLoanAmount();

        // Principal
        BigDecimal downPayment = loan.getDownPayment();

        // CMHC INSURANCE
        BigDecimal cmhcInsurance = calculatePremium(propertyValue, calculateDownPaymentPercentage(loan.getDownPayment(), propertyValue));
        calculatedAmounts.setCmhcInsurance(cmhcInsurance);

        // SET PRINCIPAL
        // loan.setPrincipal(propertyValue - loan.getDownPayment() + cmhcInsurance);
        loan.setPrincipal(propertyValue.subtract(loan.getDownPayment()).add(cmhcInsurance));

        // SET ANNUAL INTEREST RATE
        // Complete Precision
        loan.setAnnualInterestRate(loan.getAnnualInterestRate().divide(BigDecimal.valueOf(100), MathContext.DECIMAL64));

        // CALCULATE EQUAL PERIODIC PAYMENT
        BigDecimal periodicPayment = calculatePeriodicPayment(loan);

        // SET PERIODIC PAYMENT
        loan.setPeriodicPayment(periodicPayment);

        // TOTAL INTEREST
        loan.setTotalInterest(periodicPayment.multiply(BigDecimal.valueOf(loan.getLoanTermMonths())).subtract(loan.getPrincipal()));

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
            maxTaxRebate = getMaxTaxRebate(loan.getProvince().toString(), loan.getMunicipality());
        } else if (loan.isNewHomeBuyer()) {
            maxTaxRebate = getMaxTaxRebate(loan.getProvince().toString());
        }

        BigDecimal totalTaxes = provincialLandTransferTax.add(municipalLandTransferTax);
        // Rebate cannot exceed total taxes
        if (maxTaxRebate.compareTo(totalTaxes) > 0) {
            maxTaxRebate = totalTaxes;
        }

        calculatedAmounts.setLandTransferTaxRebate(maxTaxRebate);

        // PROVINCIAL SALES TAX [ PST ]
        BigDecimal pst = calculatePST(cmhcInsurance, loan.getProvince().toString());
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

        // SET TOTAL PAYMENT
//        loan.setTotalPayment(loan.getPeriodicPayment() * loan.getLoanTermMonths());
        loan.setTotalPayment(loan.getPeriodicPayment().multiply(BigDecimal.valueOf(loan.getLoanTermMonths())));
        // START DATE TODAY IF NULL
        if (loan.getStartDate() == null) {
            loan.setStartDate(LocalDate.now());
        }

        // END DATE
        LocalDate endDate = calculateEndDate(loan.getStartDate(), loan.getLoanTermMonths() / 12);
        loan.setEndDate(endDate);

        log.debug("LOAN CALCULATION COMPLETED....");

        List<AmortizationPayment> payment = generateAmortizationSchedule(loan);
        // log.debug("Amortization Schedule: {}", payment);
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
            //  PMT = P * r * (1 + r)^n / (1 + r)^n - 1
            BigDecimal r = annualRate.divide(compoundingFrequencyBD, MathContext.DECIMAL64);
            BigDecimal n = termInYears.multiply(paymentsPerYearBD);
            BigDecimal cf = one.add(r).pow(n.intValue(), MathContext.DECIMAL64);
            BigDecimal numerator = principal.multiply(r).multiply(cf);
            BigDecimal denominator = cf.subtract(one);
            BigDecimal PMT = numerator.divide(denominator, MathContext.DECIMAL64);
            return PMT;

        } catch (ArithmeticException e) {
//            System.out.println("Error in calculating periodic payment: " + e.getMessage());
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


    // REBATE
    private BigDecimal getMaxTaxRebate(String province) {
        log.debug("GETTING MAXIMUM TAX REBATE FOR PROVINCE: {}", province);
        return getMaxTaxRebate(province, "");
    }

    // REBATE OVERLOADED
    public BigDecimal getMaxTaxRebate(String province, String municipality) {
        log.debug("GETTING MAXIMUM TAX REBATE FOR PROVINCE: {} AND MUNICIPALITY: {}", province, municipality);
        return switch (province) {
            case "BC" -> BigDecimal.valueOf(8000.0);
            case "ON" -> {
                double provincialRebate = 4000.0;
                double municipalRebate = switch (municipality.toUpperCase()) {
                    case "TORONTO" -> 4475.0;
                    default -> 0.0;
                };
                yield BigDecimal.valueOf(provincialRebate + municipalRebate);
            }
            case "PE" -> BigDecimal.valueOf(2000.0);
            default -> BigDecimal.ZERO;
//            throw new IllegalArgumentException("Invalid province code: " + province);
        };
    }


    // CMHC Insurance
    public BigDecimal calculatePremium(BigDecimal totalLoanAmount, BigDecimal downPaymentPercentage) {
        log.debug("CALCULATING CMHC INSURANCE PREMIUM....");
        // Validate input
        log.debug("VALIDATING INPUT....");
        if (totalLoanAmount == null || totalLoanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total loan amount must be greater than zero.");
        }
        if (downPaymentPercentage == null ||
                downPaymentPercentage.compareTo(BigDecimal.ZERO) <= 0 ||
                downPaymentPercentage.compareTo(BigDecimal.valueOf(20)) > 0
        ) {
            throw new IllegalArgumentException("Down payment percentage must be between 0% and 20%.");
        }
        log.debug("INPUT VALIDATED....");

        log.debug("Loan Amount: {}", totalLoanAmount);
        log.debug("Down Payment Percentage: {}", downPaymentPercentage);

        NavigableMap<BigDecimal, BigDecimal> premiumRates = new TreeMap<>();
        premiumRates.put(BigDecimal.valueOf(5), new BigDecimal("0.04"));
        premiumRates.put(BigDecimal.valueOf(10), new BigDecimal("0.031"));
        premiumRates.put(BigDecimal.valueOf(15), new BigDecimal("0.028"));

        // ****  PREMIUM RATE  *****
        // floorEntry method of TreeMap:
        // FIRST get List of KEYs that are less than and equal >= SEARCHED KEY
        // Then returns the key with the highest value
        BigDecimal premiumRate = premiumRates.floorEntry(downPaymentPercentage).getValue();
        log.debug("Premium Rate: {}", premiumRate);

        // ****  PREMIUM  *****
        BigDecimal premium = totalLoanAmount.multiply(premiumRate);
        log.debug("Premium: {}", premium);

        premium = premium.setScale(2, RoundingMode.HALF_UP);
        return premium;
    }


    // PST
    public BigDecimal calculatePST(BigDecimal cmhcAmount, String province) {
        log.debug("CALCULATING PROVINCIAL SALES TAX....");
        double rate = switch (province.toUpperCase()) {
            case "MB" -> 0.07;
            case "ON" -> 0.08;
            case "QC" -> 0.09;
            case "SK" -> 0.06;
            default -> 0.0;
        };
//        return cmhcAmount * rate;
        return cmhcAmount.multiply(BigDecimal.valueOf(rate));
    }


    // Calculate end date based on start date and loan term
    public LocalDate calculateEndDate(@NotNull LocalDate startDate, int loanTermYears) {
        log.debug("CALCULATING END DATE....");
        return startDate.plusYears(loanTermYears);
    }


    // Calculate down payment percentage (optional utility)
    public BigDecimal calculateDownPaymentPercentage(BigDecimal downPayment, BigDecimal totalLoanAmount) {
        log.debug("CALCULATING DOWN PAYMENT PERCENTAGE....");
        log.debug("Down Payment: {}", downPayment);
        return downPayment.divide(totalLoanAmount, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }


    // Generate Amortization Schedule
    public List<AmortizationPayment> generateAmortizationSchedule(Loan loan) {
        List<AmortizationPayment> schedule = new ArrayList<>();

        // Extract necessary data from the loan
        BigDecimal remainingBalance = loan.getPrincipal();
        BigDecimal periodicPayment = loan.getPeriodicPayment();
        int paymentsPerYear = loanHelpers.noOfPaymentsPerYear(loan.getPaymentFrequency());
        BigDecimal periodicInterestRate = loan.getAnnualInterestRate()
                .divide(BigDecimal.valueOf(paymentsPerYear), MathContext.DECIMAL64);
        int totalPayments = loan.getLoanTermMonths();

        LocalDate startDate = loan.getStartDate();
        int startYear = startDate.getYear();

        BigDecimal TotalPaid = BigDecimal.ZERO;

        BigDecimal yearlyTotalPaid = BigDecimal.ZERO;
        BigDecimal yearlyPrincipalPaid = BigDecimal.ZERO;
        BigDecimal yearlyInterestPaid = BigDecimal.ZERO;

        BigDecimal tolerance = BigDecimal.valueOf(0.01);

        // Initial State when nothing is paid
        schedule.add(AmortizationPayment.builder()
                .year(startYear)
                .totalPaid(0.0)
                .principalPaid(0.0)
                .interestPaid(0.0)
                .remainingBalance(remainingBalance.doubleValue())
                .loan(loan)
                .build());

        log.debug("Period: 0, Current Year: {}, Total Paid: {}, Total Interest: {}, Total Principal: {}, Remaining Balance: {}",
                startYear, TotalPaid, yearlyInterestPaid, yearlyPrincipalPaid, remainingBalance);

        // Iterate over the loan term
        for (int period = 1; period <= totalPayments; period++) {

            BigDecimal interestPaid = remainingBalance.multiply(periodicInterestRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPaid = periodicPayment.subtract(interestPaid).setScale(2, RoundingMode.HALF_UP);

            // Adjust remaining balance
            if (remainingBalance.compareTo(principalPaid) <= 0) {
                interestPaid = remainingBalance.multiply(periodicInterestRate).setScale(2, RoundingMode.HALF_UP);
                principalPaid = remainingBalance;
                periodicPayment = principalPaid.add(interestPaid);
                remainingBalance = BigDecimal.ZERO;
            } else {
                remainingBalance = remainingBalance.subtract(principalPaid).setScale(2, RoundingMode.HALF_UP);
            }

            // Accumulate yearly totals
            yearlyInterestPaid = yearlyInterestPaid.add(interestPaid);
            yearlyPrincipalPaid = yearlyPrincipalPaid.add(principalPaid);
            yearlyTotalPaid = yearlyTotalPaid.add(periodicPayment);

            TotalPaid = TotalPaid.add(periodicPayment);

            // Add yearly entry at the end of each year or when loan is fully paid
            if (period % paymentsPerYear == 0 || remainingBalance.compareTo(tolerance) <= 0) {
                int year = startYear + period / paymentsPerYear;

                log.debug("Period: {}, Current Year: {}, Total Paid: {}, Total Interest: {}, Total Principal: {}, Remaining Balance: {}",
                        period, year, TotalPaid, yearlyInterestPaid, yearlyPrincipalPaid, remainingBalance);

                schedule.add(AmortizationPayment.builder()
                        .year(year)
                        .totalPaid(TotalPaid.doubleValue())
                        .principalPaid(yearlyPrincipalPaid.doubleValue())
                        .interestPaid(yearlyInterestPaid.doubleValue())
                        .remainingBalance(remainingBalance.doubleValue())
                        .loan(loan)
                        .build());

                // Reset yearly totals
                yearlyInterestPaid = BigDecimal.ZERO;
                yearlyPrincipalPaid = BigDecimal.ZERO;
                yearlyTotalPaid = BigDecimal.ZERO;

                // Break if balance is fully paid
                if (remainingBalance.compareTo(tolerance) <= 0) {
                    break;
                }
            }
        }
        return schedule;
    }


}
