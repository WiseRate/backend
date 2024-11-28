package com.wiserate.controller;

import com.wiserate.dto.loan.DemoLoan;
import com.wiserate.dto.loan.LoanResponseData;
import com.wiserate.dto.loan.NewLoanRequestData;
import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.models.Loan;
import com.wiserate.services.BankRatesService;
import com.wiserate.services.LoanService;
import com.wiserate.services.MUserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class WRController {

    // private final Logger log = LoggerFactory.getLogger(WRController.class);
    private final LoanService loanService;
    private final MUserService mUserService;
    private final BankRatesService bankRatesService;


    public WRController(
            LoanService loanService,
            MUserService mUserService, BankRatesService bankRatesService
    ) {
        this.loanService = loanService;
        this.mUserService = mUserService;
        this.bankRatesService = bankRatesService;
    }

    // @GetMapping("/")

    @GetMapping("/demo")
    public ResponseEntity<Loan> demo() {
        Loan loan = DemoLoan.createDemoLoan();
        return ResponseEntity.ok(loan);
    }

    @PostMapping("/loan")
    public ResponseEntity<?> newLoan(@Valid @RequestBody NewLoanRequestData newLoanRequestData, Authentication authentication) {
        try {
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
        } catch (Exception e) {
            log.error("FAILED TO CREATE LOAN....");
            return ResponseEntity.badRequest().body("Failed to create loan");
        }
    }

    @GetMapping("/bank-rates")
    public ResponseEntity<?> getBankRates() {
        try {
            log.debug("GETTING BANK RATES....");
            return ResponseEntity.ok(bankRatesService.getBankRates());
        } catch (Exception e) {
            log.error("FAILED TO GET BANK RATES....");
            return ResponseEntity.badRequest().body("Failed to get bank rates");
        }
    }
}




















