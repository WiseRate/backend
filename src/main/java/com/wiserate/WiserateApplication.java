package com.wiserate;

import com.wiserate.models.BankRates;
import com.wiserate.models.MUser;
import com.wiserate.enums.MUserRoles;
import com.wiserate.services.BankRatesService;
import com.wiserate.services.MUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WiserateApplication implements CommandLineRunner {

	private final MUserService mUserService;
	private final BankRatesService bankRatesService;

	@Autowired
    public WiserateApplication(MUserService mUserService, BankRatesService bankRatesService) {
        this.mUserService = mUserService;
        this.bankRatesService = bankRatesService;
    }

    public static void main(String[] args) {
		SpringApplication.run(WiserateApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// CREATE TEST USER
		MUser user = new MUser("jackson", "jackson", "jackson@gmail.com");
		MUser admin = new MUser("jack", "jackson", "jackson@gmail.com", MUserRoles.ADMIN);

		BankRates TD = BankRates.builder().provider("TD").threeYearFixed(2.5).fiveYearFixed(3.5).tenYearFixed(4.5).fiveYearVariable(2.5).build();
		BankRates Scotia = BankRates.builder().provider("Scotia").threeYearFixed(2.6).fiveYearFixed(3.6).tenYearFixed(4.6).fiveYearVariable(2.6).build();
		BankRates RBC = BankRates.builder().provider("RBC").threeYearFixed(2.7).fiveYearFixed(3.7).tenYearFixed(4.7).fiveYearVariable(2.7).build();
		BankRates BMO = BankRates.builder().provider("BMO").threeYearFixed(2.8).fiveYearFixed(3.8).tenYearFixed(4.8).fiveYearVariable(2.8).build();
		BankRates CIBC = BankRates.builder().provider("CIBC").threeYearFixed(2.9).fiveYearFixed(3.9).tenYearFixed(4.9).fiveYearVariable(2.9).build();



		mUserService.createMUser(user);
		mUserService.createMUser(admin);


		bankRatesService.addBankRates(TD);
		bankRatesService.addBankRates(Scotia);
		bankRatesService.addBankRates(RBC);
		bankRatesService.addBankRates(BMO);
		bankRatesService.addBankRates(CIBC);

	}

}
