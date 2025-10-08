package com.example.WigellRepairService.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails simon = User.withUsername("simon")
                .password("{noop}simon")
                .roles("ADMIN")
                .build();

        UserDetails alex = User.withUsername("alex")
                .password("{noop}alex")
                .roles("USER")
                .build();

        UserDetails sara = User.withUsername("sara")
                .password("{noop}sara")
                .roles("USER")
                .build();

        UserDetails amanda = User.withUsername("amanda")
                .password("{noop}amanda")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(simon, alex, sara, amanda);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        String[] ADMIN_ENDPOINTS = {
                "/api/wigellrepairs/listcanceled",
                "/api/wigellrepairs/listupcoming",
                "/api/wigellrepairs/listpast",
                "/api/wigellrepairs/addservice",
                "/api/wigellrepairs/updateservice",
                "/api/wigellrepairs/removeservice/**",
                "/api/wigellrepairs/addtechnician",
                "/api/wigellrepairs/technicians"
        };

        String[] USER_ENDPOINTS = {
                "/api/wigellrepairs/services",
                "/api/wigellrepairs/bookservice",
                "/api/wigellrepairs/cancelbooking",
                "/api/wigellrepairs/mybookings"
        };

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(USER_ENDPOINTS).hasRole("USER")
                        .requestMatchers("/api/wigellrepairs/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
