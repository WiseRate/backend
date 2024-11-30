package com.wiserate.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "amortization_payment")
@Builder
@ToString
public class AmortizationPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Double totalPaid;

    @Column(nullable = false)
    private Double principalPaid;

    @Column(nullable = false)
    private Double interestPaid;

    @Column(nullable = false)
    private Double remainingBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_loan_id", nullable = false)
    private Loan loan;
}
