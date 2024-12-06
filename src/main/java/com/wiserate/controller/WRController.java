package com.wiserate.controller;

import com.wiserate.dto.loan.DemoLoan;
import com.wiserate.dto.loan.LoanResponseData;
import com.wiserate.dto.loan.NewLoanRequestData;
import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.models.Loan;
import com.wiserate.services.BankRatesService;
import com.wiserate.services.LoanService;
import com.wiserate.services.MUserService;
import com.wiserate.services.PdfGeneratorService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class WRController {

    // private final Logger log = LoggerFactory.getLogger(WRController.class);
    private final LoanService loanService;
    private final MUserService mUserService;
    private final BankRatesService bankRatesService;
    private final PdfGeneratorService pdfGeneratorService;


    public WRController(
            LoanService loanService,
            MUserService mUserService, BankRatesService bankRatesService,
            PdfGeneratorService pdfGeneratorService
    ) {
        this.loanService = loanService;
        this.mUserService = mUserService;
        this.bankRatesService = bankRatesService;
        this.pdfGeneratorService = pdfGeneratorService;
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

    @GetMapping("/bank-rates-simple")
    public ResponseEntity<?> getBankRatesSimple() {
        try {
            log.debug("GETTING BANK RATES....");
            Map<String, Float> bankRates = new HashMap<>();
            bankRates.put("RBC", 2.99f);
            bankRates.put("TD", 3.29f);
            bankRates.put("BMO", 3.49f);
            bankRates.put("Scotia", 3.69f);
            bankRates.put("CIBC", 3.89f);
            return ResponseEntity.ok(bankRates);
        } catch (Exception e) {
            log.error("FAILED TO GET BANK RATES....");
            return ResponseEntity.badRequest().body("Failed to get bank rates");
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

    @GetMapping("/generate-amortization-pdf")
    public ResponseEntity<?> generateAmortizationPdf(@Valid @RequestBody NewLoanRequestData newLoanRequestData) {
        try {
            log.debug("GENERATING AMORTIZATION PDF....");
            Loan loan = loanService.convertToLoan(newLoanRequestData);
            loan = loanService.initializeLoan(loan);
            LoanResponseData responseData = loanService.convertToLoanResponseData(loan);
            byte[] pdf = pdfGeneratorService.generateAmortizationSchedulePdf("Wiserate", responseData);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "amortization-schedule.pdf");
            headers.setContentLength(pdf.length);
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            log.error("FAILED TO GENERATE AMORTIZATION PDF....");
            return ResponseEntity.badRequest().body("Failed to generate amortization pdf");
        }
    }

    @PostMapping("/amortization-schedule")
    public ResponseEntity<?> getAmortizationSchedule(@Valid @RequestBody NewLoanRequestData newLoanRequestData) {
        try {
            log.debug("FETCHING AMORTIZATION SCHEDULE: {}", newLoanRequestData);
            Loan loan = loanService.convertToLoan(newLoanRequestData);
            loan = loanService.initializeLoan(loan);
            LoanResponseData responseData = loanService.convertToLoanResponseData(loan);
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            log.error("FAILED TO FETCH AMORTIZATION SCHEDULE....");
            return ResponseEntity.badRequest().body("Failed to fetch amortization schedule");
        }
    }


}




















