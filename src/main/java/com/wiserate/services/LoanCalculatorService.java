package com.wiserate.services;

import com.wiserate.enums.InterestType;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.models.Loan;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LoanCalculatorService {


    // convert years to months
    /*
    public int convertYearsToMonths(double years) {
        int fullYears = (int) years;
        int remainingMonths = (int) ((years - fullYears) * 12);
        return fullYears * 12 + remainingMonths;
    }
    */


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
    /**
     * Calculates equal periodic payments for a loan with compound interest.
     *
     * @param principal              Principal amount of the loan.
     * @param annualRate             Annual interest rate (as a decimal, e.g., 0.05 for 5%).
     * @param compoundingFrequency   Number of compounding periods per year.
     * @param termInYears            Loan term in years.
     * @param paymentsPerYear        Number of payment periods per year.
     * @return                       Equal periodic payment amount.
     */
    public double calculatePeriodicPaymentCompoundInterest(double principal, double annualRate, int compoundingFrequency, double termInYears, int paymentsPerYear) {
        if (principal <= 0 || annualRate <= 0 || compoundingFrequency <= 0 || termInYears <= 0 || paymentsPerYear <= 0) {
            System.out.println("Principal: " + principal + "\tAnnual Rate: " + annualRate + "\tCompounding Frequency: " + compoundingFrequency + "\tTerm in Years: " + termInYears + "\tPayments per Year: " + paymentsPerYear);
            return 0;
        }
        double effectiveRate = Math.pow(1 + annualRate / compoundingFrequency, (double) compoundingFrequency / paymentsPerYear) - 1;
        int totalPayments = (int) (termInYears * paymentsPerYear);

        System.out.println("Effective Rate: " + effectiveRate + "\tTotal Payments: " + totalPayments);

        // Calculate periodic payment using the annuity formula
        return Math.round((principal * effectiveRate) / (1 - Math.pow(1 + effectiveRate, -totalPayments))*100.0)/100.0;
    }


    // Calculate equal amount of payment for full term of loan
    public double calculatePeriodicPayment(Loan loan) {
        double  principal               = loan.getPrincipal() - loan.getDownPayment();
        double  annualRate              = loan.getAnnualInterestRate() / 100.0;

        int     termInMonths            = loan.getLoanTermMonths();
        double  termInYears             = (double) termInMonths /12;

        boolean isCompoundInterest      = loan.getIsCompoundInterest();

        int paymentsPerYear = calculatePaymentsPerYear(loan.getPaymentFrequency());

        double periodicPayment;

        if (isCompoundInterest) {

            int compoundingFrequency    = loan.getCompoundFrequency();

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
            periodicPayment = Math.round(totalAmount / totalPayments * 100.0) /100.0;
        }
//        System.out.println("Periodic Payment: " + periodicPayment);
        return periodicPayment;


    }


    // getProvincialTaxRate based on province
// Method to get the maximum tax rebate for a province
    public double getMaxTaxRebate(String province, String municipality) {
        return switch (province) {
            case "AB" -> 0.0;
            case "BC" -> 8000.0;
            case "MB" -> 0.0;
            case "NB" -> 0.0;
            case "NL" -> 0.0;
            case "NS" -> 0.0;
            case "NT" -> 0.0;
            case "NU" -> 0.0;
            case "ON" -> {
                double provincialRebate = 4000.0;
                double municipalRebate = switch (municipality.toUpperCase()) {
                    case "TORONTO" -> 4475.0;
                    default -> 0.0;
                };
                yield provincialRebate + municipalRebate;
            }
            case "PE" -> 2000.0;
            case "QC" -> 0.0;
            case "SK" -> 0.0;
            case "YT" -> 0.0;
            default -> 0.0;
//            throw new IllegalArgumentException("Invalid province code: " + province);
        };
    }






    // Calculate Land Transfer Tax Ontario
    public double calculateLandTransferTax(double propertyValue, boolean firstTimeHomeBuyer) {
        if (firstTimeHomeBuyer) return 0; // First-time home buyers are exempt from land transfer tax
        /*
        0.5% on amounts up to and including $55,000
        1.0% on amounts exceeding $55,000, up to and including $250,000
        1.5% on amounts exceeding $250,000, up to and including $400,000
        2.0% on amounts exceeding $400,000
        2.5% on amounts exceeding $2 million
         */
        double tax = 0;
        if (propertyValue <= 55000) {
            tax = propertyValue * 0.005;
        } else if (propertyValue <= 250000) {
            tax = 55000 * 0.005 + (propertyValue - 55000) * 0.01;
        } else if (propertyValue <= 400000) {
            tax = 55000 * 0.005 + (250000 - 55000) * 0.01 + (propertyValue - 250000) * 0.015;
        } else if (propertyValue <= 2000000) {
            tax = 55000 * 0.005 + (250000 - 55000) * 0.01 + (400000 - 250000) * 0.015 + (propertyValue - 400000) * 0.02;
        } else {
            tax = 55000 * 0.005 + (250000 - 55000) * 0.01 + (400000 - 250000) * 0.015 + (2000000 - 400000) * 0.02 + (propertyValue - 2000000) * 0.025;
        }
        return tax;
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
    public LocalDate calculateEndDate(LocalDate startDate, int loanTermYears) {
        return startDate.plusYears(loanTermYears);
    }

    // Calculate down payment percentage (optional utility)
    public double calculateDownPaymentPercentage(double downPayment, double totalLoanAmount) {
        return (downPayment / totalLoanAmount) * 100;
    }


}
