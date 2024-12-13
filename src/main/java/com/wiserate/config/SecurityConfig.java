package com.wiserate.config;


import com.wiserate.enums.MUserRoles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
// Telling Spring that Register this class in Spring Context
@EnableWebSecurity(debug = false)
// It tells Spring that treat this class as Spring Security configuration class.
// Activates default Security
// Enable Custom defined Configuration from this class
@EnableMethodSecurity
// Enable Method Level Security
public class SecurityConfig {

    private static final String[] AUTH_WHITELIST = {
            "/user/create",
            "/api/v1/loan",
            "/h2-console/**",
            "/api/v1/bank-rates",
            "/api/v1/bank-rates-simple",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/api/v1/generate-amortization-pdf",
            "/api/v1/amortization-schedule",
            "/error"
    };

    private static final String[] AUTH_WHITELIST_FRONTEND = {
            "/", "/index.html", "/favicon.ico", "/static/**",
            "/{path:^(?!api).*$}"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        String[] merged_array = Stream.concat(Arrays.stream(AUTH_WHITELIST), Arrays.stream(AUTH_WHITELIST_FRONTEND)).toArray(String[]::new);

        http
                .authorizeHttpRequests((requests) -> {
                    requests
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()     // Allow all OPTIONS requests for CORS
//                           .requestMatchers("/h2-console/**").permitAll()
//                             .requestMatchers("/admin/**").hasAnyRole(String.valueOf(MUserRoles.ADMIN))
                            .requestMatchers(merged_array).permitAll()
                            .anyRequest().authenticated();
                    // .anyRequest().permitAll();
                });

        //  by making stateless we don't have to remember user state,
        //  each request will be verified independently.
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // CSRF Protection is enabled by default in Spring Security.
        // We need to disable it for our REST API as we are not using cookies for session management.
        // Also, our session management is stateless.
        http.csrf(csrf -> csrf.ignoringRequestMatchers(merged_array));
        http.csrf(AbstractHttpConfigurer::disable);

        http.headers((headers) -> headers.defaultsDisabled() // Disable default headers
                .frameOptions((frameOptions) -> frameOptions.sameOrigin())); // Allow iframes for same-origin requests

        http.exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(customAuthenticationEntryPoint));
        http.httpBasic((httpBasic) -> httpBasic.authenticationEntryPoint(customAuthenticationEntryPoint));
        return http.build();
    }

    // ALLOWING CORS FOR OUR FRONTEND APPLICATION
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        WebMvcConfigurer webMvcConfigurer = new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000", "https://wiserate-b64eaf61bfea.herokuapp.com", "http://127.0.0.1:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                ;

            }
        };
        return webMvcConfigurer;
    }

    //  Password Encrypt
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    //  InMemoryUserManager
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user1 = User.withUsername("admin").password(passwordEncoder().encode("admin")).roles(String.valueOf(MUserRoles.ADMIN)).build();
//        UserDetailsService uDS = new InMemoryUserDetailsManager(user1);
//        return uDS;
//    }

    // IGNORING SECURITY
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        //  ONE WAY ->
        //  return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/images/**");

        //  ANOTHER WAY ->
        WebSecurityCustomizer webSecurityCustomizer = new WebSecurityCustomizer() {
            @Override
            public void customize(WebSecurity web) {
                web.ignoring().requestMatchers(
                        // FRONTEND STATIC FILES
                        "/css/**",
                        "/js/**"
                );
            }
        };
        return webSecurityCustomizer;
    }


}
