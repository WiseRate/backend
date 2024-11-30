package com.wiserate.helpers;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class LandTransferTax {

    public BigDecimal calculate(BigDecimal propertyValue, String province){
        switch (province.toUpperCase()) {
//            case "ALBERTA":
//                return alberta(propertyValue);
//            case "BRITISH_COLUMBIA":
//                return britishColumbia(propertyValue);
//            case "MANITOBA":
//                return manitoba(propertyValue);
//            case "NEW_BRUNSWICK":
//                return newBrunswick(propertyValue);
//            case "NEWFOUNDLAND_AND_LABRADOR":
//                return newfoundlandAndLabrador(propertyValue);
//            case "NOVA_SCOTIA":
//                return novaScotia(propertyValue);
            case "ON":
                return ontario(propertyValue);
//            case "PRINCE_EDWARD_ISLAND":
//                return princeEdwardIsland(propertyValue);
//            case "QUEBEC":
//                return quebec(propertyValue);
//            case "SASKATCHEWAN":
//                return saskatchewan(propertyValue);
//            case "NORTHWEST_TERRITORIES":
//                return northwestTerritories(propertyValue);
//            case "NUNAVUT":
//                return nunavut(propertyValue);
//            case "YUKON":
//                return yukon(propertyValue);
            default:
                return BigDecimal.ZERO;
        }

    }

    public BigDecimal ontario(BigDecimal propertyValue) {
        /*
        Tax rates:
        0.5% on amounts up to and including $55,000
        1.0% on amounts exceeding $55,000, up to and including $250,000
        1.5% on amounts exceeding $250,000, up to and including $400,000
        2.0% on amounts exceeding $400,000
        2.5% on amounts exceeding $2 million
        */

        BigDecimal tax;
        BigDecimal limit1 = BigDecimal.valueOf(55000);
        BigDecimal limit2 = BigDecimal.valueOf(250000);
        BigDecimal limit3 = BigDecimal.valueOf(400000);
        BigDecimal limit4 = BigDecimal.valueOf(2000000);

        BigDecimal rate1 = new BigDecimal("0.005"); // 0.5%
        BigDecimal rate2 = new BigDecimal("0.01");  // 1.0%
        BigDecimal rate3 = new BigDecimal("0.015"); // 1.5%
        BigDecimal rate4 = new BigDecimal("0.02");  // 2.0%
        BigDecimal rate5 = new BigDecimal("0.025"); // 2.5%

        if (propertyValue.compareTo(limit1) <= 0) {
            tax = propertyValue.multiply(rate1);
        } else if (propertyValue.compareTo(limit2) <= 0) {
            tax = limit1.multiply(rate1)
                    .add(propertyValue.subtract(limit1).multiply(rate2));
        } else if (propertyValue.compareTo(limit3) <= 0) {
            tax = limit1.multiply(rate1)
                    .add(limit2.subtract(limit1).multiply(rate2))
                    .add(propertyValue.subtract(limit2).multiply(rate3));
        } else if (propertyValue.compareTo(limit4) <= 0) {
            tax = limit1.multiply(rate1)
                    .add(limit2.subtract(limit1).multiply(rate2))
                    .add(limit3.subtract(limit2).multiply(rate3))
                    .add(propertyValue.subtract(limit3).multiply(rate4));
        } else {
            tax = limit1.multiply(rate1)
                    .add(limit2.subtract(limit1).multiply(rate2))
                    .add(limit3.subtract(limit2).multiply(rate3))
                    .add(limit4.subtract(limit3).multiply(rate4))
                    .add(propertyValue.subtract(limit4).multiply(rate5));
        }

        return tax.setScale(2, RoundingMode.HALF_UP);
    }

}
