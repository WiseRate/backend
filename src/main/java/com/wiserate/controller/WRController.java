package com.wiserate.controller;

import com.wiserate.dto.loan.DemoLoan;
import com.wiserate.dto.loan.LoanResponseData;
import com.wiserate.dto.loan.MapToLoan;
import com.wiserate.dto.loan.NewLoanRequestData;
import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.enums.InterestType;
import com.wiserate.enums.LoanTypes;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import com.wiserate.models.CalculatedAmounts;
import com.wiserate.models.Fees;
import com.wiserate.models.Loan;
import com.wiserate.services.LoanService;
import com.wiserate.services.MUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
public class WRController {

    private final LoanService loanService;
    private final MUserService mUserService;
    private final MapToLoan mapToLoan;

    public WRController(
            LoanService loanService,
            MUserService mUserService,
            MapToLoan mapToLoan
    ) {
        this.loanService = loanService;
        this.mUserService = mUserService;
        this.mapToLoan = mapToLoan;
    }

    @GetMapping("/demo")
    public ResponseEntity<Loan> demo() {
        Loan loan = DemoLoan.createDemoLoan();
        return ResponseEntity.ok(loan);
    }

    // check authentication
    // convert newLoanRequestData to Loan
    // call initializeLoan
    // this will return Loan with all calculated values
    // save Loan to DB
    // convert Loan to LoanResponseData
    // return LoanResponseData

    @PostMapping("/loan")
    public ResponseEntity<?> newLoan(@Valid @RequestBody NewLoanRequestData newLoanRequestData, Authentication authentication) {
//        System.out.println("New Loan Request Data Received: ");
        UserDTO user = mUserService.validateUser(authentication);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        Loan loan = mapToLoan.mapObject(newLoanRequestData);
        loan = loanService.initializeLoan(loan);
        loan.setUser(mUserService.getUserById(user.getId()));
        LoanResponseData responseData = loanService.saveLoan(loan);
        return ResponseEntity.ok(responseData);
    }
}




















