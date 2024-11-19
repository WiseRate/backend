package com.wiserate.repository;

import com.wiserate.models.MUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MUserRepository extends JpaRepository<MUser, Long> {

    public Optional<MUser> findByUsername(String username);

}
