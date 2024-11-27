package com.wiserate.models;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Embeddable
public class Fees {
    private BigDecimal insurancePremium = BigDecimal.ZERO;      //   700
    private BigDecimal lawyerFee = BigDecimal.ZERO;             //  1000
    private BigDecimal appraisalFee = BigDecimal.ZERO;          //   300
    private BigDecimal homeInspectionFee = BigDecimal.ZERO;     //   500
    private BigDecimal otherFees = BigDecimal.ZERO;
    private BigDecimal titleInsurance = BigDecimal.ZERO;
    private BigDecimal propertyTax = BigDecimal.ZERO;

    @Override
    public String toString() {
        return "Fees: {" +
                "insurancePremium=" + insurancePremium +
                ", lawyerFee=" + lawyerFee +
                ", appraisalFee=" + appraisalFee +
                ", homeInspectionFee=" + homeInspectionFee +
                ", otherFees=" + otherFees +
                ", titleInsurance=" + titleInsurance +
                ", propertyTax=" + propertyTax +
                '}';
    }
}
