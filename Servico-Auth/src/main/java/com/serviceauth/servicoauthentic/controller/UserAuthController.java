package com.serviceauth.servicoauthentic.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.serviceauth.servicoauthentic.dto.LoginDTO;
import com.serviceauth.servicoauthentic.entity.User;
import com.serviceauth.servicoauthentic.repository.UserRepository;
import com.serviceauth.servicoauthentic.service.security.jwt.JwtUtils;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UserAuthController {



    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AuthenticationManager authenticationManager;

    @PostMapping("/auth/register")
    public ResponseEntity<?> registierUser(@RequestBody User user){

        if(userRepository.existsByUsername(user.getUsername())){

            return ResponseEntity.badRequest().body("Username already exists");

        }

        if(userRepository.existsByEmail(user.getEmail())){

            return ResponseEntity.badRequest().body("Email is already being used");

        }

        User newUser = new User();
        newUser.setFullname(user.getFullname());
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));


        userRepository.save(newUser);

        return ResponseEntity.ok(Map.of("message", "User created successfully"));

    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO){

        try {
            Authentication authentication =authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generationJwtToken(authentication);

            return ResponseEntity.ok(Map.of("token", jwt));

        } catch (BadCredentialsException ex) {

            return ResponseEntity.status(401).body("Invalid username or password");

        }
    }


    @GetMapping("/user")
    public ResponseEntity<List<User>> getAllUser(@RequestBody User user){

        List<User> existUser = userRepository.findAll();


        return new ResponseEntity<>(existUser,HttpStatus.OK);

    }


    @GetMapping("/id/{username}")
    public ResponseEntity<Optional<User>> getUserIdByUsername(@PathVariable String username) {
        Optional<User> userId = userRepository.findByUsername(username);

        if (userId != null) {
            return ResponseEntity.ok(userId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
