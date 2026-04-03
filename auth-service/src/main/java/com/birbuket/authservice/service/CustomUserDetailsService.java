package com.birbuket.authservice.service;

import com.birbuket.authservice.exception.UserIsNotActive;
import com.birbuket.authservice.exception.UserNotFoundException;
import com.birbuket.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        if (!user.isEnabled()) {
            throw new UserIsNotActive("User " + username + " is disabled");
        }

        return user;
    }
}
