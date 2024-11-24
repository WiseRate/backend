package com.wiserate.models;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Embeddable
public class CalculatedAmounts {
    private BigDecimal municipalLandTransferTax = BigDecimal.ZERO;
    private BigDecimal provincialLandTransferTax = BigDecimal.ZERO;
    private BigDecimal landTransferTaxRebate = BigDecimal.ZERO;
    private BigDecimal provincialSalesTax = BigDecimal.ZERO;
    private BigDecimal cmhcInsurance = BigDecimal.ZERO;
}

