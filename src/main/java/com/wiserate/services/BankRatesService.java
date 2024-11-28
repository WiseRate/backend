package com.wiserate.services;

import com.wiserate.models.BankRates;
import com.wiserate.repository.BankRatesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BankRatesService {

    private final BankRatesRepository bankRatesRepository;

    public BankRatesService(BankRatesRepository bankRatesRepository) {
        this.bankRatesRepository = bankRatesRepository;
    }

    // get all bank rates
    public List<BankRates> getBankRates() {
        return bankRatesRepository.findAll();
    }

    // get bank rates by provider
    public BankRates getBankRatesByProvider(String provider) {
        return bankRatesRepository.findByProvider(provider);
    }

    // add bank rates
    public BankRates addBankRates(BankRates bankRates) {
        return bankRatesRepository.save(bankRates);
    }

    // update bank rates
    public BankRates updateBankRates(BankRates bankRates) {
        return bankRatesRepository.save(bankRates);
    }
}
