package com.bellagnech.dig_bank.security;

import com.bellagnech.dig_bank.security.jwt.JwtAuthenticationFilter;
import com.bellagnech.dig_bank.security.jwt.JwtAuthorizationFilter;
import com.bellagnech.dig_bank.security.jwt.JwtUtil;
import com.bellagnech.dig_bank.security.services.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] WHITE_LIST_URLS = {
            "/swagger-ui/**", 
            "/v3/api-docs/**", 
            "/h2-console/**",
            "/login",
            "/api/auth/**",
            "/error"
    };
    
    /**
     * Configure DaoAuthenticationProvider with our custom UserDetailsService
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                  AuthenticationManager authenticationManager,
                                                  JwtUtil jwtUtil,
                                                  ObjectMapper objectMapper,
                                                  DaoAuthenticationProvider authenticationProvider) throws Exception {
        // Create JWT authentication filter
        JwtAuthenticationFilter jwtAuthenticationFilter = 
            new JwtAuthenticationFilter(authenticationManager, jwtUtil, objectMapper);
        jwtAuthenticationFilter.setFilterProcessesUrl("/api/auth/login");
        
        // Create JWT authorization filter
        JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtUtil);
        
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(WHITE_LIST_URLS).permitAll()
                // Allow read-only operations for authenticated users
                .requestMatchers(HttpMethod.GET, "/customers/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/accounts/**").authenticated()
                // Restrict administrative operations to ADMIN role
                .requestMatchers(HttpMethod.POST, "/customers/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/customers/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/customers/**").hasRole("ADMIN")
                // Account operations require specific roles
                .requestMatchers(HttpMethod.POST, "/accounts/current").hasAnyRole("ADMIN", "ACCOUNT_MANAGER")
                .requestMatchers(HttpMethod.POST, "/accounts/saving").hasAnyRole("ADMIN", "ACCOUNT_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/accounts/**/status").hasAnyRole("ADMIN", "ACCOUNT_MANAGER")
                // Allow transactions for authenticated users with TELLER role
                .requestMatchers(HttpMethod.POST, "/accounts/**/debit").hasAnyRole("ADMIN", "TELLER", "ACCOUNT_MANAGER")
                .requestMatchers(HttpMethod.POST, "/accounts/**/credit").hasAnyRole("ADMIN", "TELLER", "ACCOUNT_MANAGER")
                .requestMatchers(HttpMethod.POST, "/accounts/transfer").hasAnyRole("ADMIN", "TELLER", "ACCOUNT_MANAGER")
                // Default rule: require authentication for all other requests
                .anyRequest().authenticated())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> 
                headers.frameOptions(frameOptions -> frameOptions.disable())) // For H2 console
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + 
                        authException.getMessage() + "\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Access Denied\",\"message\":\"" + 
                        accessDeniedException.getMessage() + "\"}");
                }))
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/logout"))
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Logout successful\"}");
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true))
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
