package com.helpflow.ticket.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ProblemDetailSecurityHandlers securityHandlers;

    /**
     * /actuator/prometheus: además del JWT de ADMIN acepta HTTP Basic con una credencial técnica
     * (rol METRICS), porque Prometheus no sabe hacer login ni renovar un JWT. Sin contraseña
     * configurada no existe usuario técnico y solo entra el ADMIN.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain prometheusFilterChain(HttpSecurity http,
                                                     @Value("${helpflow.metrics.scraper.username}") String username,
                                                     @Value("${helpflow.metrics.scraper.password:}") String password)
            throws Exception {
        InMemoryUserDetailsManager scraperUsers = new InMemoryUserDetailsManager();
        if (!password.isBlank()) {
            scraperUsers.createUser(User.withUsername(username)
                    .password(passwordEncoder().encode(password))
                    .roles("METRICS")
                    .build());
        }
        return http
                .securityMatcher(EndpointRequest.to(PrometheusScrapeEndpoint.class))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .userDetailsService(scraperUsers)
                .httpBasic(b -> b.authenticationEntryPoint(securityHandlers.authenticationEntryPoint()))
                .authorizeHttpRequests(auth -> auth.anyRequest().hasAnyRole("ADMIN", "METRICS"))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(securityHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(securityHandlers.accessDeniedHandler()))
                .addFilterBefore(jwtAuthenticationFilter, BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // API stateless con JWT en header: no hay cookie de sesión que proteger -> CSRF no aplica.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(securityHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(securityHandlers.accessDeniedHandler()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** El filtro JWT es un @Component: evitamos que Spring Boot lo registre además como filtro de servlet global. */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
