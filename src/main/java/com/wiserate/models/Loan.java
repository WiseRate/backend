package com.wiserate.models;

import com.wiserate.enums.InterestType;
import com.wiserate.enums.LoanTypes;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanTypes loanType;                 // Home loan for now.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProvinceCA province;                // Province of the loan.

    @Column(nullable = false)
    private Double totalLoanAmount;             // 11000/22000/55000 etc.

    @Column(nullable = false)
    private Double downPayment;                 // 1000/2000/5000 etc.

    @Column(nullable = false)
    private Double principal;                   // 10000/20000/50000 etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType interestType;          // 1 = Fixed, 2 = Variable

    @Column(nullable = false)
    private Boolean isCompoundInterest = true;  // Compound or simple interest.

    @Column(nullable = false)
    private int compoundFrequency = 2;          // 1/2/4/12 etc.

    @Column(nullable = false)
    private Double annualInterestRate;          // 0.05/0.10/0.15 etc.

    @Column(nullable = false)
    private int loanTermMonths;               // 6/12/24/36 etc. How many months the loan is for.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentFrequency paymentFrequency;      // Number of payments per year.

    @Column(nullable = false)
    private LocalDate startDate;                // 2021-01-01
    private LocalDate endDate;                  // 2021-12-31               [calculated]

    private Double payment;                     // Regular payment amount.

    private Double totalInterest;               // Total interest paid over the term [calculated].
    private Double totalPayment;                // Total payment amount (principal + interest) [calculated].

    private Double insurancePremium = 0.0;      // Optional: Monthly insurance for loan protection.
    private Double propertyTax = 0.0;           // Optional: Monthly property tax (for home loans).

    @Column(nullable = false)
    private Boolean isActive = false;                    // Active or not.

    @UpdateTimestamp
    private LocalDateTime lastUpdated;

//    private Double residualValue;       // For car loans: Residual value (if applicable).

    // user can have multiple loans
    // user linked via fk_user_id [i.e. user_id]
    // Cascade not needed here as we are not saving User object from Loan object
    // FetchType.LAZY is used to load the user object only when it is needed
    //      else .Eager is used to load the full user object every time
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="fk_user_id", nullable = false)
    private MUser user;
}
