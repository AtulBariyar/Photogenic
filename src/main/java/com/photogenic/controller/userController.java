package com.photogenic.controller;

import com.photogenic.dto.userLoginDto;
import com.photogenic.dto.userRegisterDto;
import com.photogenic.model.userModel;
import com.photogenic.utility.jwtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/")
public class userController {

       @Autowired
    private com.photogenic.service.userService userService;

    @Autowired
    private com.photogenic.repository.userRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private jwtUtility jwtUtility;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody userRegisterDto userRegisterDto) {
        try {
            if (userService.usernameExists(userRegisterDto.getUsername())) {
                return new ResponseEntity<>("Username already registered!", HttpStatus.CONFLICT);
            }
            userRegisterDto.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
            userModel createdUser = userService.registerUser(userRegisterDto);

            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Registration failed due to an error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody userLoginDto userLoginDTO) {
        try {
            String loginResponse = userService.loginUser(userLoginDTO);
            if (loginResponse.contains("Login successful")) {
                return ResponseEntity.ok(loginResponse);
            } else {
                return ResponseEntity.status(401).body(loginResponse);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Login failed due to an error.");
        }
    }
}
