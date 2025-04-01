package br.com.shopping.infrastructure.config;

import br.com.shopping.infrastructure.security.JwtAuthenticationFilter;
import br.com.shopping.infrastructure.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class JwtFilterConfig {

    @Bean
    public OncePerRequestFilter jwtAuthenticationFilter(
            JwtService jwtService, 
            UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }
}