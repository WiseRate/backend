package com.wiserate.dto.loan;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AmortizationPaymentDTO {
    private Integer year;
    private Double totalPaid;
    private Double principalPaid;
    private Double interestPaid;
    private Double remainingBalance;
}
