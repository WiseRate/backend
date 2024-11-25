package com.wiserate.services;

import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import com.wiserate.helpers.LandTransferTax;
import com.wiserate.models.CalculatedAmounts;
import com.wiserate.models.Fees;
import com.wiserate.models.Loan;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class LoanCalculatorService {

    private final Logger log = LoggerFactory.getLogger(LoanCalculatorService.class);
    private final LandTransferTax landTransferTax;


    public LoanCalculatorService(LandTransferTax landTransferTax) {
        this.landTransferTax = landTransferTax;
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

        // RETURN LOAN
        return loan;
    }


    // function that convert PaymentFrequency to frequencyFactor
    private int calculatePaymentsPerYear(PaymentFrequency frequency) {
        log.debug("Calculating payments per year for frequency: {}", frequency);
        return switch (frequency) {
            case DAILY -> 365;
            case WEEKLY -> 52;
            case BIWEEKLY -> 26;
            case MONTHLY -> 12;
            case QUARTERLY -> 4;
            case SEMIANNUALLY -> 2;
            case ANNUALLY -> 1;
            default -> 0;
        };
    }


    // Calculating equal periodic payment for compound interest when compounding frequency is different from payment frequency and principal is given
    /*
        public BigDecimal calculatePeriodicPaymentCompoundInterest(BigDecimal principal, BigDecimal annualRate, int compoundingFrequency, BigDecimal termInYears, int paymentsPerYear) {
            if (principal <= 0 || annualRate <= 0 || compoundingFrequency <= 0 || termInYears <= 0 || paymentsPerYear <= 0) {
                System.out.println("Principal: " + principal + "\tAnnual Rate: " + annualRate + "\tCompounding Frequency: " + compoundingFrequency + "\tTerm in Years: " + termInYears + "\tPayments per Year: " + paymentsPerYear);
                return 0;
            }
            double effectiveRate = Math.pow(1 + annualRate / compoundingFrequency, (double) compoundingFrequency / paymentsPerYear) - 1;
            int totalPayments = (int) (termInYears * paymentsPerYear);
            double equalPayments = Math.round((principal * effectiveRate) / (1 - Math.pow(1 + effectiveRate, -totalPayments)) * 100.0) / 100.0;
            return equalPayments;
    }
    */
    public BigDecimal calculatePeriodicPaymentCompoundInterest(
            BigDecimal principal,
            BigDecimal annualRate,
            int compoundingFrequency,
            BigDecimal termInYears,
            int paymentsPerYear) {
        log.debug("CALCULATING PERIODIC PAYMENT FOR COMPOUND INTEREST....");
//        log.debug("Principal: {}", principal);
//        log.debug("Annual Rate: {}", annualRate);
//        log.debug("Compounding Frequency: {}", compoundingFrequency);
//        log.debug("Term in Years: {}", termInYears);
//        log.debug("Payments per Year: {}", paymentsPerYear);

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
            BigDecimal ratePerCompoundingPeriod = annualRate.divide(compoundingFrequencyBD, MathContext.DECIMAL64);
            BigDecimal power = compoundingFrequencyBD.divide(paymentsPerYearBD, MathContext.DECIMAL64);
            double fractionalPower = power.doubleValue();
            BigDecimal effectiveRate = BigDecimal.valueOf(
                            Math.pow(one.add(ratePerCompoundingPeriod).doubleValue(), fractionalPower))
                    .subtract(one);

            log.debug("Effective Rate: {}", effectiveRate);

            // Total number of payments
            int totalPayments = termInYears.multiply(paymentsPerYearBD).intValue();

            BigDecimal numerator = principal.multiply(effectiveRate);

            BigDecimal denominator = one.subtract(
                    one.add(effectiveRate).pow(-totalPayments, MathContext.DECIMAL64));

//            log.debug("Numerator: {}", numerator);
//            log.debug("Denominator: {}", denominator);

            if (denominator.compareTo(BigDecimal.ZERO) == 0) {
                log.error("Error in calculating periodic payment: Division by zero");
                return BigDecimal.ZERO;
            }

            return numerator.divide(denominator, 2, RoundingMode.HALF_UP);

        } catch (ArithmeticException e) {
//            System.out.println("Error in calculating periodic payment: " + e.getMessage());
            log.error("Error in calculating periodic payment: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }


    // Calculate equal amount of payment for full term of loan
    public BigDecimal calculatePeriodicPayment(Loan loan) {
        log.debug("INITIALIZING PERIODIC PAYMENT CALCULATION....");

        BigDecimal principal = loan.getPrincipal();
        BigDecimal annualRate = loan.getAnnualInterestRate();

        int termInMonths = loan.getLoanTermMonths();
        BigDecimal termInYears = BigDecimal.valueOf(termInMonths / 12.0);

        boolean isCompoundInterest = loan.getIsCompoundInterest();

        int paymentsPerYear = calculatePaymentsPerYear(loan.getPaymentFrequency());

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
        if (downPaymentPercentage.compareTo(BigDecimal.ZERO) < 0 || downPaymentPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Down payment percentage must be between 0 and 100.");
        }

        // Determine the premium rate based on the down payment percentage
        BigDecimal premiumRate = BigDecimal.ZERO;
//        if (downPaymentPercentage >= 5 && downPaymentPercentage < 10) {
        if (downPaymentPercentage.compareTo(BigDecimal.valueOf(5)) >= 0 &&
                downPaymentPercentage.compareTo(BigDecimal.valueOf(10)) < 0) {
            premiumRate = new BigDecimal("0.4"); // 4.00%
        } else if (downPaymentPercentage.compareTo(BigDecimal.valueOf(10)) >= 0 &&
                downPaymentPercentage.compareTo(BigDecimal.valueOf(15)) < 0) {
            premiumRate = new BigDecimal("0.031"); // 3.10%
        } else if (downPaymentPercentage.compareTo(BigDecimal.valueOf(15)) >= 0 &&
                downPaymentPercentage.compareTo(BigDecimal.valueOf(20)) < 0) {
            premiumRate = new BigDecimal("0.028"); // 2.80%
        }

        BigDecimal premium = totalLoanAmount.multiply(premiumRate);
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
        return downPayment.divide(totalLoanAmount, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }


}
