package com.wiserate.controller;

import com.wiserate.dto.mUser.UserCreationRequest;
import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.dto.mUser.UpdateRequest;
import com.wiserate.exceptions.mUser.ErrorResponse;
import com.wiserate.exceptions.mUser.UnauthorizedException;
import com.wiserate.models.MUser;
import com.wiserate.services.MUserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    // private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final MUserService mUserService;

    public UserController(MUserService mUserService) {
        this.mUserService = mUserService;
    }

    // LOGIN
    @GetMapping("/login")
    public ResponseEntity<?> login(Authentication authentication) {
        log.debug("LOGIN REQUEST RECEIVED....");
        try {
            UserDTO user = mUserService.validateUser(authentication);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            log.error("LOGIN FAILED....");
            ErrorResponse errorResponse = new ErrorResponse("Login failed", e.getMessage(), HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    // GET ALL USERS
    @GetMapping("/all")
    public ResponseEntity<?> getUsers(Authentication authentication) {
        log.debug("GETTING ALL USERS....");
        try {
            return new ResponseEntity<>(mUserService.getMUsers(authentication), HttpStatus.OK);
        } catch (UnauthorizedException e) {
            log.error("UNAUTHORIZED REQUEST....");
            ErrorResponse errorResponse = new ErrorResponse("Unauthorized", e.getMessage(), HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("FAILED TO GET USERS....");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // GET USER BY USERNAME
    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username, Authentication authentication) {
        log.debug("GETTING USER: {}", username);
        try {
            UserDTO user = mUserService.getMUser(username, authentication);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            log.error("USER NOT FOUND....");
            ErrorResponse errorResponse = new ErrorResponse("User not found", e.getMessage(), HttpStatus.NOT_FOUND.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    // CREATE USER
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserCreationRequest userCreationRequest) {
        log.debug("CREATING USER: {}", userCreationRequest.getUsername());
        try {
            MUser user = new MUser(userCreationRequest.getUsername(), userCreationRequest.getPassword(), userCreationRequest.getEmail());
            return new ResponseEntity<>(mUserService.createMUser(user), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("FAILED TO CREATE USER....");
            ErrorResponse errorResponse = new ErrorResponse("failed", e.getMessage(), HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // UPDATE USER
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UpdateRequest updateRequest, Authentication authentication) {
        log.debug("UPDATING USER: {}", updateRequest.getId());
        try {
            UserDTO user = mUserService.updateUser(updateRequest, authentication);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            log.error("USER UPDATE FAILED....");
            ErrorResponse errorResponse = new ErrorResponse("User update failed", e.getMessage(), HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
