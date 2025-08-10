package com.photogenic.service;


import com.photogenic.dto.userLoginDto;
import com.photogenic.dto.userRegisterDto;
import com.photogenic.model.pgModel;
import com.photogenic.model.userModel;
import com.photogenic.utility.jwtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.photogenic.repository.userRepository;

@Component
@Service
public class userService {
    @Autowired
    private userRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private jwtUtility jwtUtility;
    public boolean usernameExists(String username) {
        userModel existingUser = userRepository.findByUsername(username);
        return existingUser != null;
    }

    public userModel getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void saveUser(userModel user){
         userRepository.save(user);
        }

    // Register a new user
    public userModel registerUser(userRegisterDto userRegisterDto) {
        userModel userEntity = new userModel();
        userEntity.setUsername(userRegisterDto.getUsername());
        userEntity.setPassword(userRegisterDto.getPassword());
        userEntity.setEmail(userRegisterDto.getEmail());
        return userRepository.save(userEntity);
    }

    public String loginUser(userLoginDto userLoginDTO) {
        try {
            userModel user = userRepository.findByUsername(userLoginDTO.getUsername());
            if (user == null) {
                return "User not found!";
            }

            if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
                return "Invalid password!";
            }

            String accessToken = jwtUtility.generateAccessToken(user.getUsername());
            //String refreshToken = jwtUtility.generateRefreshToken(user.getUsername());

            return "Login successful! \n Access Token :"+accessToken;
        } catch (Exception e) {
            e.printStackTrace();
            return "Login failed due to an error.";
        }
    }
}
//+ " \nRefresh Token:  + refreshToken"