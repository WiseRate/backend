package com.wiserate.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class BankRates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String provider;            // TD/Scotia/RBC/BMO/CIBC

    private double threeYearFixed;
    private double fiveYearFixed;
    private double tenYearFixed;

    private double fiveYearVariable;

    @UpdateTimestamp                    // Automatically updates the lastUpdated field with the current timestamp
    private LocalDateTime lastUpdated;

}
