package com.banyan.workmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.banyan.workmanagement.model.User;
import com.banyan.workmanagement.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                // Get permissions from role and convert to GrantedAuthority list
                List<SimpleGrantedAuthority> authorities = user.getRole().getPermissions().stream()
                                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                                .collect(Collectors.toList());

                // Also add the role itself as an authority (optional but common)
                authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));

                return new org.springframework.security.core.userdetails.User(
                                user.getUsername(),
                                user.getPassword(),
                                authorities);
        }
}
