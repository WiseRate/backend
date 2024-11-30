package com.wiserate;

import com.wiserate.enums.*;
import com.wiserate.models.BankRates;
import com.wiserate.models.Fees;
import com.wiserate.models.Loan;
import com.wiserate.models.MUser;
import com.wiserate.services.BankRatesService;
import com.wiserate.services.LoanService;
import com.wiserate.services.MUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootApplication
public class WiserateApplication implements CommandLineRunner {

	private final MUserService mUserService;
	private final BankRatesService bankRatesService;
	private final LoanService loanService;

	@Autowired
    public WiserateApplication(MUserService mUserService, BankRatesService bankRatesService, LoanService loanService) {
        this.mUserService = mUserService;
        this.bankRatesService = bankRatesService;
        this.loanService = loanService;
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

		/*
		{
			"loanType": "HOME_LOAN",
			"province": "ON",
			"municipality": "toronto",
			"totalLoanAmount": 200000.0,
			"downPayment": 10000.0,
			"interestType": "VARIABLE",
			"isCompoundInterest": true,
			"compoundFrequency": 2,
			"annualInterestRate": 3.99,
			"loanTermMonths": 300,
			"paymentFrequency": "MONTHLY",
			"newHomeBuyer": true,
			"startDate": "2030-01-01",
			"fees": {
				"insurancePremium": 0.0,
				"lawyerFee": 1000.0,
				"appraisalFee": 300.0,
				"homeInspectionFee": 500.0,
				"otherFees": 0.0,
				"titleInsurance": 900.0,
				"propertyTax": 0.0
			},
			"isActive": "true"
		}
		 */

		Fees fees = Fees.builder()
				.insurancePremium(BigDecimal.valueOf(0.0))
				.lawyerFee(BigDecimal.valueOf(1000.0))
				.appraisalFee(BigDecimal.valueOf(300.0))
				.homeInspectionFee(BigDecimal.valueOf(500.0))
				.otherFees(BigDecimal.valueOf(0.0))
				.titleInsurance(BigDecimal.valueOf(900.0))
				.propertyTax(BigDecimal.valueOf(0.0))
				.build();

		Loan loan = Loan.builder()
				.loanType(LoanTypes.HOME_LOAN)
				.province(ProvinceCA.ON)
				.municipality("toronto")
				.totalLoanAmount(BigDecimal.valueOf(200000.0))
				.downPayment(BigDecimal.valueOf(10000.0))
				.interestType(InterestType.VARIABLE)
				.isCompoundInterest(true)
				.compoundFrequency(2)
				.annualInterestRate(BigDecimal.valueOf(3.99))
				.loanTermMonths(300)
				.paymentFrequency(PaymentFrequency.MONTHLY)
				.newHomeBuyer(true)
				.startDate(LocalDate.parse("2024-01-01"))
				.fees(fees)
				.isActive(true)
				.build();






		mUserService.createMUser(user);
		mUserService.createMUser(admin);


		bankRatesService.addBankRates(TD);
		bankRatesService.addBankRates(Scotia);
		bankRatesService.addBankRates(RBC);
		bankRatesService.addBankRates(BMO);
		bankRatesService.addBankRates(CIBC);

		Loan initLoan = loanService.initializeLoan(loan);
		initLoan.setUser(mUserService.getUserById(user.getId()));
		loanService.saveLoan(loan);



	}

}
