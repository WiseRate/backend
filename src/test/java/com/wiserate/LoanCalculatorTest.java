package com.wiserate;

import com.wiserate.enums.LoanTypes;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import com.wiserate.helpers.LandTransferTax;
import com.wiserate.models.Loan;
import com.wiserate.services.LoanCalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
public class LoanCalculatorTest {

    // Create an instance of the class that contains the function
    private final LoanCalculatorService loanCalculator;
    private final LandTransferTax landTransferTax;


    public LoanCalculatorTest(LoanCalculatorService loanCalculator, LandTransferTax landTransferTax) {
        System.out.println("HERE 1");
        this.loanCalculator = loanCalculator;
        this.landTransferTax = landTransferTax;
        System.out.println("HERE 2");
    }

    @Test
    void testCalculatePeriodicPayment_simpleInterest() {
        Loan loan = new Loan();

        loan.setLoanType(LoanTypes.HOME_LOAN); // Set to any valid loan type
        loan.setProvince(ProvinceCA.ON); // Example: Ontario
        loan.setPrincipal(100000.0);          // Principal amount
        loan.setAnnualInterestRate(5.0);      // Annual interest rate (as percentage)
        loan.setLoanTermMonths(360);          // Loan term in months (e.g., 30 years)
        loan.setCompoundFrequency(12);        // Compound frequency (e.g., monthly compounding)
        loan.setIsCompoundInterest(false);    // Use simple interest for this test
        loan.setPaymentFrequency(PaymentFrequency.MONTHLY); // Monthly payments
        loan.setStartDate(LocalDate.of(2021, 1, 1));        // Loan start date

        double expectedPayment = 694.44;
        double actualPayment = loanCalculator.calculatePeriodicPayment(loan);
        System.out.println("Expected payment: " + expectedPayment);
        System.out.println("Actual payment: " + actualPayment);
        assertEquals(expectedPayment, actualPayment, 0.01, "Periodic payment does not match expected value.");
    }

    @Test
    void testCalculatePeriodicPayment_compoundInterest() {
        Loan loan = new Loan();

        loan.setLoanType(LoanTypes.HOME_LOAN); // Set to any valid loan type
        loan.setProvince(ProvinceCA.ON); // Example: Ontario
        loan.setPrincipal(100000.0);          // Principal amount
        loan.setAnnualInterestRate(5.0);      // Annual interest rate (as percentage)
        loan.setLoanTermMonths(360);          // Loan term in months (e.g., 30 years)
        loan.setCompoundFrequency(12);        // Compound frequency (e.g., monthly compounding)
        loan.setIsCompoundInterest(true);     // Use compound interest for this test
        loan.setPaymentFrequency(PaymentFrequency.MONTHLY); // Monthly payments
        loan.setStartDate(LocalDate.of(2021, 1, 1));        // Loan start date

        double expectedPayment = 536.82;
        Loan loan1 = loanCalculator.initialize(loan);
        double actualPayment = loan1.getPeriodicPayment();
        System.out.println("Expected payment: " + expectedPayment);
        System.out.println("Actual payment: " + actualPayment);
        assertEquals(expectedPayment, actualPayment, 0.01, "Periodic payment does not match expected value.");
    }

    @Test
    void testCalculatePeriodicPaymentCompoundInterest() {
        // Input values
        double principal = 100000; // Example principal amount
        double annualRate = 5; // Annual interest rate (5%)
        int compoundingFrequency = 12; // Compounded monthly
        double termInYears = 30; // Loan term (30 years)
        int paymentsPerYear = 12; // Monthly payments

        // Expected value (calculated separately or via a reliable tool)
        double expectedPayment = 536.82; // Replace with accurate value

        // Call the function
        double actualPayment = loanCalculator.calculatePeriodicPaymentCompoundInterest(
                principal,
                annualRate / 100, // Convert to decimal
                compoundingFrequency,
                termInYears,
                paymentsPerYear
        );
        System.out.println("Expected payment: " + expectedPayment);
        System.out.println("Actual payment: " + actualPayment);

        // Assert the result
        assertEquals(expectedPayment, actualPayment, 0.01, "Periodic payment does not match expected value.");
    }

    @Test
    void testEdgeCaseZeroPrincipal() {
        double principal = 0; // Edge case: Zero principal
        double annualRate = 5;
        int compoundingFrequency = 12;
        double termInYears = 10;
        int paymentsPerYear = 12;

        // Expect zero payment
        double actualPayment = loanCalculator.calculatePeriodicPaymentCompoundInterest(
                principal, annualRate / 100, compoundingFrequency, termInYears, paymentsPerYear
        );

        assertEquals(0, actualPayment, "Payment for zero principal should be zero.");
    }

    @Test
    void testEdgeCaseShortTerm() {
        double principal = 50000;
        double annualRate = 10;
        int compoundingFrequency = 12;
        double termInYears = 0.5; // 6 months
        int paymentsPerYear = 12;

        // Expected payment calculation or placeholder
        double expectedPayment = 8578.07;

        double actualPayment = loanCalculator.calculatePeriodicPaymentCompoundInterest(
                principal, annualRate / 100, compoundingFrequency, termInYears, paymentsPerYear
        );

        assertEquals(expectedPayment, actualPayment, 0.01, "Short-term payment mismatch.");
    }


    @Test
    void testCalculateLandTransferTax() {
        double price = 1000000; // Example property price
        ProvinceCA province = ProvinceCA.ON; // Example province
        double expectedTax = 32950; // Expected land transfer tax

        double actualTax = landTransferTax.ontario(price);
        System.out.println("Expected tax: " + expectedTax);
        System.out.println("Actual tax: " + actualTax);

        assertEquals(expectedTax, actualTax, 0.01, "Land transfer tax does not match expected value.");
    }
}
