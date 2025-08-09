package com.banyan.workmanagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.banyan.workmanagement.service.CustomUserDetailsService;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize etc.
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService userDetailsService;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/login", "/css/**", "/js/**", "/api/backup/**")
                                                .permitAll()
                                                .requestMatchers("/add-user").hasAuthority("SUPERADMIN")
                                                .requestMatchers("/manage-role-access/**")
                                                .hasAnyAuthority("SUPERADMIN")

                                                .requestMatchers("/saveCustomer").hasAuthority("CUSTOMER_SAVE")

                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .permitAll())
                                // CSRF configuration - disable for attendance endpoints
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/attendance/check-in",
                                                                "/attendance/check-out",
                                                                "/attendance/delete",
                                                                "/attendance/delete/**",
                                                                "/attendance/save"))
                                // REMEMBER ME CONFIGURATION
                                .rememberMe(rememberMe -> rememberMe
                                                .key("aVerySecretKey123456") // Use a strong secret in production!
                                                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
                                                .userDetailsService(userDetailsService));

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                // Plain text passwords — NOT recommended for production. Use
                // BCryptPasswordEncoder in production.
                return NoOpPasswordEncoder.getInstance();
        }
}
