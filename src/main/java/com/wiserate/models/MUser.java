package com.wiserate.models;


import com.wiserate.enums.MUserRoles;
import com.wiserate.exceptions.mUser.UserDataIncompleteException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Builder
public class MUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    private MUserRoles role;

    @CreationTimestamp                // Automatically set the current date and time by Hibernate
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modifiedAt", insertable = false)
    private LocalDateTime modifiedAt;

    public MUser(String username, String password, String email) {
        if (username == null || password == null || email == null) {
            throw new UserDataIncompleteException("Username, password, and email must not be null");
        }
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public MUser(String username, String password, String email, MUserRoles role) {
        if (username == null || password == null || role == null || email == null) {
            throw new UserDataIncompleteException("Username, password, role, and email must not be null");
        }
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    @Override
    public String toString() {
        return "MUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                '}';
    }
}
