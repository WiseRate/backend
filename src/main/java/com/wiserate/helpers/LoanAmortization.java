package com.wiserate.helpers;

import com.wiserate.models.AmortizationPayment;
import com.wiserate.models.Loan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LoanAmortization {

    private final LoanHelpers loanHelpers;

    public LoanAmortization(LoanHelpers loanHelpers) {
        this.loanHelpers = loanHelpers;
    }

    // Generate Amortization Schedule
    public List<AmortizationPayment> generateAmortizationSchedule(Loan loan) {
        List<AmortizationPayment> schedule = new ArrayList<>();

        // Extract necessary data from the loan
        BigDecimal remainingBalance = loan.getPrincipal();
        BigDecimal periodicPayment = loan.getPeriodicPayment();


        // Annual interest rate as a decimal
        double annualRateDecimal = loan.getAnnualInterestRate().doubleValue();

        // Compounding frequency per year (e.g., 12 for monthly)
        int compoundingFrequency = loan.getCompoundFrequency();

        // Payment frequency per year (e.g., 52 for weekly)
        int paymentsPerYear = loanHelpers.noOfPaymentsPerYear(loan.getPaymentFrequency());

        // Calculate the nominal interest rate per compounding period
        double r_c = annualRateDecimal / compoundingFrequency;

        // Calculate the exponent representing the ratio of compounding periods to payment periods
        double exponent = (double) compoundingFrequency / paymentsPerYear;

        // Calculate the periodic interest rate per payment period
        double periodicRate = Math.pow(1 + r_c, exponent) - 1;

        // Convert back to BigDecimal
        BigDecimal periodicInterestRate = BigDecimal.valueOf(periodicRate);

        int termToYears = (int) Math.round((double) loan.getLoanTermMonths() / 12);
        int totalPayments = termToYears * paymentsPerYear;

        LocalDate startDate = loan.getStartDate();
        int startYear = startDate.getYear();

        BigDecimal TotalPaid = BigDecimal.ZERO;

        BigDecimal yearlyTotalPaid = BigDecimal.ZERO;
        BigDecimal yearlyPrincipalPaid = BigDecimal.ZERO;
        BigDecimal yearlyInterestPaid = BigDecimal.ZERO;

        BigDecimal tolerance = BigDecimal.valueOf(0.01);

        // Iterate over the loan term
        for (int period = 1; period < totalPayments + 1; period++) {
            // log.debug("Calculating for period: {}", period);

            BigDecimal interestPaid = remainingBalance.multiply(periodicInterestRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPaid = periodicPayment.subtract(interestPaid).setScale(2, RoundingMode.HALF_UP);
            // remainingBalance = remainingBalance.subtract(principalPaid).setScale(2, RoundingMode.HALF_UP);

            // Check if this is the last payment (remaining balance is less than periodicPayment)
            if (remainingBalance.compareTo(periodicPayment) <= 0 && period == totalPayments) {
                principalPaid = periodicPayment.subtract(remainingBalance.multiply(periodicInterestRate).setScale(2, RoundingMode.HALF_UP));
                remainingBalance = BigDecimal.ZERO; // Set remaining balance to zero
            } else {
                remainingBalance = remainingBalance.subtract(principalPaid).setScale(2, RoundingMode.HALF_UP);
            }

            yearlyInterestPaid = yearlyInterestPaid.add(interestPaid);
            yearlyPrincipalPaid = yearlyPrincipalPaid.add(principalPaid);
            yearlyTotalPaid = yearlyTotalPaid.add(periodicPayment);

            TotalPaid = TotalPaid.add(periodicPayment);

            if (period % paymentsPerYear == 0 || remainingBalance.compareTo(tolerance) <= 0) {
                int year = (startYear + period / paymentsPerYear) - 1;

                if (remainingBalance.compareTo(BigDecimal.ZERO) < 0 && remainingBalance.abs().compareTo(tolerance) <= 0) {
                    remainingBalance = BigDecimal.ZERO;
                }

                schedule.add(AmortizationPayment.builder()
                        .year(year)
                        .totalPaid(yearlyTotalPaid.setScale(2, RoundingMode.HALF_UP).doubleValue())
                        .principalPaid(yearlyPrincipalPaid.setScale(2, RoundingMode.HALF_UP).doubleValue())
                        .interestPaid(yearlyInterestPaid.setScale(2, RoundingMode.HALF_UP).doubleValue())
                        .remainingBalance(remainingBalance.setScale(2, RoundingMode.HALF_UP).doubleValue())
                        .loan(loan)
                        .build());


                yearlyInterestPaid = BigDecimal.ZERO;
                yearlyPrincipalPaid = BigDecimal.ZERO;
                yearlyTotalPaid = BigDecimal.ZERO;

                if (remainingBalance.compareTo(BigDecimal.ZERO) == 0) {
                    break;
                }
            }
        }
        return schedule;
    }
}
