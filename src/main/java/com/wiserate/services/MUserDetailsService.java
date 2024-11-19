package com.wiserate.services;

import com.wiserate.models.MUser;
import com.wiserate.repository.MUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MUserDetailsService implements UserDetailsService {

    private final MUserRepository mUserRepository;

    @Autowired
    public MUserDetailsService(MUserRepository mUserRepository) {
        this.mUserRepository = mUserRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<MUser> findingUser = mUserRepository.findByUsername(username);
        if (findingUser.isEmpty()) throw new UsernameNotFoundException("USER NOT FOUND");
        MUser foundUser = findingUser.get();
        return User
                .builder()
                .username(foundUser.getUsername())
                .password(foundUser.getPassword())
                .roles(String.valueOf(foundUser.getRole()))
                .build();
    }
}
