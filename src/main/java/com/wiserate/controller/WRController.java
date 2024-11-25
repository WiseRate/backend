package com.wiserate.controller;

import com.wiserate.dto.loan.DemoLoan;
import com.wiserate.dto.loan.LoanResponseData;
import com.wiserate.dto.loan.NewLoanRequestData;
import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.models.Loan;
import com.wiserate.services.LoanService;
import com.wiserate.services.MUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class WRController {

    private final Logger log = LoggerFactory.getLogger(WRController.class);
    private final LoanService loanService;
    private final MUserService mUserService;


    public WRController(
            LoanService loanService,
            MUserService mUserService
    ) {
        this.loanService = loanService;
        this.mUserService = mUserService;
    }

    @GetMapping("/demo")
    public ResponseEntity<Loan> demo() {
        Loan loan = DemoLoan.createDemoLoan();
        return ResponseEntity.ok(loan);
    }

    @PostMapping("/loan")
    public ResponseEntity<?> newLoan(@Valid @RequestBody NewLoanRequestData newLoanRequestData, Authentication authentication) {
        log.error("LOAN REQUEST RECEIVED....");
        UserDTO user = null;
        if (authentication != null && authentication.isAuthenticated()) {
            user = mUserService.validateUser(authentication);
            if (user == null) {
                log.error("USER NOT FOUND....");
                return ResponseEntity.badRequest().body("User not found");
            }
            log.debug("USER VALIDATED....");
        }
        Loan loan = loanService.convertToLoan(newLoanRequestData);
        loan = loanService.initializeLoan(loan);
        if (user == null) {
            log.debug("USER NOT LOGGED IN | SKIPPED DB SAVE....");
            LoanResponseData responseData = loanService.convertToLoanResponseData(loan);
            return ResponseEntity.ok(responseData);
        }
        log.debug("USER LOGGED IN....");
        loan.setUser(mUserService.getUserById(user.getId()));
        LoanResponseData responseData = loanService.convertToLoanResponseData(loanService.saveLoan(loan));
        return ResponseEntity.ok(responseData);
    }
}




















