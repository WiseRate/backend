package com.wiserate.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class BankRates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;            // TD/Scotia/RBC/BMO/CIBC

    private Double threeYearFixed;
    private Double fiveYearFixed;
    private Double tenYearFixed;

    private Double fiveYearVariable;

    @UpdateTimestamp                    // Automatically updates the lastUpdated field with the current timestamp
    private LocalDateTime lastUpdated;

    @Override
    public String toString() {
        return "BankRates{" +
                "id=" + id +
                ", provider='" + provider + '\'' +
                ", threeYearFixed=" + threeYearFixed +
                ", fiveYearFixed=" + fiveYearFixed +
                ", tenYearFixed=" + tenYearFixed +
                ", fiveYearVariable=" + fiveYearVariable +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
