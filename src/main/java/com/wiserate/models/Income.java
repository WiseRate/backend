package com.wiserate.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Income {
    // amount
    // frequency [weekly, bi-weekly, monthly, yearly, one-time]
    // source [salary, bonus, investment, other]

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;
    private String frequency;
    private String source;

    @ManyToOne
    @JoinColumn(name="fk_user_id")
    private MUser user;

    @Override
    public String toString() {
        return "Income{" +
                "id=" + id +
                ", amount=" + amount +
                ", frequency='" + frequency + '\'' +
                ", source='" + source + '\'' +
                ", user=" + user +
                '}';
    }
}
