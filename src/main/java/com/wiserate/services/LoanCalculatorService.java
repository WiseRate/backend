package com.wiserate.services;

import com.wiserate.enums.InterestType;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import com.wiserate.helpers.LandTransferTax;
import com.wiserate.models.Loan;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LoanCalculatorService {

    private final LandTransferTax landTransferTax;

    public LoanCalculatorService(LandTransferTax landTransferTax) {
        this.landTransferTax = landTransferTax;
    }

    // initialize all data into the Loan object
    public Loan initialize(Loan loan) {

        // PROPERTY REAL VALUE
        double propertyValue = loan.getTotalLoanAmount();

        // CMHC INSURANCE
        double cmhcInsurance = calculatePremium(propertyValue, calculateDownPaymentPercentage(loan.getDownPayment(), propertyValue));
        loan.setCmhcInsurance(cmhcInsurance);

        // SET PRINCIPAL
        loan.setPrincipal(propertyValue - loan.getDownPayment() + cmhcInsurance);

        // SET ANNUAL INTEREST RATE
        loan.setAnnualInterestRate(loan.getAnnualInterestRate() / 100.0);

        // CALCULATE EQUAL PERIODIC PAYMENT
        double periodicPayment = calculatePeriodicPayment(loan);

        // SET PERIODIC PAYMENT
        loan.setPeriodicPayment(periodicPayment);

        // TOTAL INTEREST
        loan.setTotalInterest(periodicPayment * loan.getLoanTermMonths() - loan.getPrincipal());

        // Set Ontario if province is null
        if (loan.getProvince() == null) {
            loan.setProvince(ProvinceCA.ON);
        }

        // SET MUNICIPALITY
        if (loan.getMunicipality() == null) {
            loan.setMunicipality("toronto");
        }

        // LAND TRANSFER TAX
        double provincialLandTransferTax = landTransferTax.calculate(propertyValue, loan.getProvince().toString());
        loan.setProvincialLandTransferTax(provincialLandTransferTax);

        // MUNICIPAL LAND TRANSFER TAX [ONLY TORONTO]
        double municipalLandTransferTax = 0.0;
        if (loan.getMunicipality().equalsIgnoreCase("toronto")) {
            municipalLandTransferTax = landTransferTax.calculate(propertyValue, "toronto");
        }
        loan.setMunicipalLandTransferTax(municipalLandTransferTax);

        // LAWYER FEE
        if (loan.getLawyerFee() == 0.0) {
            loan.setLawyerFee(1000.0);
        }

        // MAXIMUM TAX REBATE [ FIRST TIME HOME BUYER ]
        double maxTaxRebate = 0.0;
        if (loan.isNewHomeBuyer() && loan.getProvince() == ProvinceCA.ON) {
            maxTaxRebate = getMaxTaxRebate(loan.getProvince().toString(), loan.getMunicipality());
        } else if (loan.isNewHomeBuyer()) {
            maxTaxRebate = getMaxTaxRebate(loan.getProvince().toString());
        }

        // PROVINCIAL SALES TAX [ PST ]
        double pst = calculatePST(cmhcInsurance, loan.getProvince().toString());
        loan.setProvincialSalesTax(pst);

        double finalLandTransferTax = provincialLandTransferTax + municipalLandTransferTax - maxTaxRebate;

        // CASH TO CLOSE
        double cashToClose = loan.getDownPayment()
                + finalLandTransferTax
                + loan.getProvincialSalesTax()
                + loan.getLawyerFee()
                + loan.getTitleInsurance()
                + loan.getHomeInspectionFee()
                + loan.getAppraisalFee()
                + loan.getOtherFees();

        loan.setCashToClose(cashToClose);

        // SET TOTAL PAYMENT
        loan.setTotalPayment(loan.getPeriodicPayment() * loan.getLoanTermMonths());

        // START DATE TODAY IF NULL
        if (loan.getStartDate() == null) {
            loan.setStartDate(LocalDate.now());
        }

        // END DATE
        loan.setEndDate(calculateEndDate(loan.getStartDate(), loan.getLoanTermMonths() / 12));

        // RETURN LOAN
        return loan;
    }

    // function that convert PaymentFrequency to frequencyFactor
    private int calculatePaymentsPerYear(PaymentFrequency frequency) {
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
    public double calculatePeriodicPaymentCompoundInterest(double principal, double annualRate, int compoundingFrequency, double termInYears, int paymentsPerYear) {
        if (principal <= 0 || annualRate <= 0 || compoundingFrequency <= 0 || termInYears <= 0 || paymentsPerYear <= 0) {
            System.out.println("Principal: " + principal + "\tAnnual Rate: " + annualRate + "\tCompounding Frequency: " + compoundingFrequency + "\tTerm in Years: " + termInYears + "\tPayments per Year: " + paymentsPerYear);
            return 0;
        }
        double effectiveRate = Math.pow(1 + annualRate / compoundingFrequency, (double) compoundingFrequency / paymentsPerYear) - 1;
        int totalPayments = (int) (termInYears * paymentsPerYear);

        System.out.println("Effective Rate: " + effectiveRate + "\tTotal Payments: " + totalPayments);

        return Math.round((principal * effectiveRate) / (1 - Math.pow(1 + effectiveRate, -totalPayments)) * 100.0) / 100.0;
    }

    // Calculate equal amount of payment for full term of loan
    public double calculatePeriodicPayment(Loan loan) {
        double principal = loan.getPrincipal();
        double annualRate = loan.getAnnualInterestRate();

        int termInMonths = loan.getLoanTermMonths();
        double termInYears = (double) termInMonths / 12;

        boolean isCompoundInterest = loan.getIsCompoundInterest();

        int paymentsPerYear = calculatePaymentsPerYear(loan.getPaymentFrequency());

        double periodicPayment;

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
            double totalInterest = principal * annualRate * termInYears;
            double totalAmount = principal + totalInterest;
            // Total number of payments
            int totalPayments = (int) (termInMonths / (12.0 / paymentsPerYear));
            // Calculate periodic payment
            periodicPayment = Math.round(totalAmount / totalPayments * 100.0) / 100.0;
        }
        return periodicPayment;
    }


    // REBATE
    private double getMaxTaxRebate(String province) {
        return getMaxTaxRebate(province, "");
    }

    // REBATE OVERLOADED
    public double getMaxTaxRebate(String province, String municipality) {
        return switch (province) {
            case "BC" -> 8000.0;
            case "ON" -> {
                double provincialRebate = 4000.0;
                double municipalRebate = switch (municipality.toUpperCase()) {
                    case "TORONTO" -> 4475.0;
                    default -> 0.0;
                };
                yield provincialRebate + municipalRebate;
            }
            case "PE" -> 2000.0;
            default -> 0.0;
//            throw new IllegalArgumentException("Invalid province code: " + province);
        };
    }


    // CMHC Insurance
    public double calculatePremium(double totalLoanAmount, double downPaymentPercentage) {
        // Validate input
        if (downPaymentPercentage < 0 || downPaymentPercentage > 100) {
            throw new IllegalArgumentException("Down payment percentage must be between 0 and 100.");
        }

        // Determine the premium rate based on the down payment percentage
        double premiumRate = 0.0; // Default is no premium for down payment >= 20%
        if (downPaymentPercentage >= 5 && downPaymentPercentage < 10) {
            premiumRate = 0.04; // 4.00%
        } else if (downPaymentPercentage >= 10 && downPaymentPercentage < 15) {
            premiumRate = 0.031; // 3.10%
        } else if (downPaymentPercentage >= 15 && downPaymentPercentage < 20) {
            premiumRate = 0.028; // 2.80%
        }

        // Calculate the premium
        return totalLoanAmount * premiumRate;
    }


    // PST
    public double calculatePST(double cmhcAmount, String province) {
        double rate = switch (province.toUpperCase()) {
            case "MB" -> 0.07;
            case "ON" -> 0.08;
            case "QC" -> 0.09;
            case "SK" -> 0.06;
            default -> 0.0;
        };
        return cmhcAmount * rate;
    }

    // Calculate loan term (in years) based on principal, interest rate, and monthly payment
    public double calculateLoanTerm(double principal, double annualInterestRate, double monthlyPayment) {
        double monthlyRate = (annualInterestRate / 100) / 12; // Convert annual rate to monthly

        // Formula for loan term: n = log(Payment / (Payment - Principal × rate)) / log(1 + rate)
        return Math.log(monthlyPayment / (monthlyPayment - principal * monthlyRate))
                / Math.log(1 + monthlyRate) / 12;
    }

    // Calculate total interest paid over the loan term
    public double calculateTotalInterest(double principal, double monthlyPayment, int loanTermYears) {
        double totalPayments = loanTermYears * 12;
        return (monthlyPayment * totalPayments) - principal; // Total paid - Principal
    }

    // Calculate total payment (Principal + Total Interest)
    public double calculateTotalPayment(double monthlyPayment, int loanTermYears) {
        int totalPayments = loanTermYears * 12;
        return monthlyPayment * totalPayments;
    }

    // Calculate end date based on start date and loan term
    public LocalDate calculateEndDate(@NotNull LocalDate startDate, int loanTermYears) {
        return startDate.plusYears(loanTermYears);
    }

    // Calculate down payment percentage (optional utility)
    public double calculateDownPaymentPercentage(double downPayment, double totalLoanAmount) {
        double percent = (downPayment / totalLoanAmount) * 100;
        return Math.round(percent * 100.0) / 100.0;
    }


}
