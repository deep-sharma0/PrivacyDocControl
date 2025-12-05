package com.privacydoccontrol.config;

import com.privacydoccontrol.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @SuppressWarnings("deprecation")
    @Bean
    public DaoAuthenticationProvider authProvider(CustomUserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/register/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/upload", "/token/**").permitAll() // allow upload/token check without login
                .requestMatchers("/files/**").authenticated() // must be logged in to preview files
                .requestMatchers("/staff/**").hasRole("STAFF")
                .requestMatchers("/user/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customAuthSuccessHandler()) // redirect based on role
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/staff/print/**", "/staff/delete/**") // allow print/delete without CSRF
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

            if (authorities.contains("ROLE_STAFF")) {
                response.sendRedirect("/staff/view");
            } else if (authorities.contains("ROLE_USER")) {
                response.sendRedirect("/dashboard");
            } else {
                response.sendRedirect("/login?error");
            }
        };
    }
}