package com.wiserate.controller;

import com.wiserate.services.LoanService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class WRController {

    private final LoanService loanService;

    public WRController(LoanService loanService) {
        this.loanService = loanService;
    }
}
