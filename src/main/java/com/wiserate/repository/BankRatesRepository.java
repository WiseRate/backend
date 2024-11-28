package com.wiserate.repository;

import com.wiserate.models.BankRates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRatesRepository extends JpaRepository<BankRates, Long> {

    BankRates findByProvider(String provider);
}
