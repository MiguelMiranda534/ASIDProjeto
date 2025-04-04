package com.serviceauth.servicoauthentic.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.serviceauth.servicoauthentic.entity.User;
import com.serviceauth.servicoauthentic.repository.UserRepository;

@Service
public class UserServiceImpl implements UserDetailsService{
    
    @Autowired
    private UserRepository userRepository;

    public UserDetails loadUserByUsername(String username){

        User user = userRepository.findByUsername(username).orElse(null);


        if(user == null){

            throw new UsernameNotFoundException("User not found with the given username");
        }


        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .build();
    }
}
