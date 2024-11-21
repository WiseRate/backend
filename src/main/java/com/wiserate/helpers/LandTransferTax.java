package com.wiserate.helpers;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class LandTransferTax {

    public double calculate(double propertyValue, String province){
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
            case "ONTARIO":
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
                return 0;
        }

    }

    public double ontario(double propertyValue) {
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

}
