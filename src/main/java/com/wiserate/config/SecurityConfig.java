package com.wiserate.config;


import com.wiserate.enums.MUserRoles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
// Telling Spring that Register this class in Spring Context
@EnableWebSecurity(debug = false)
// It tells Spring that treat this class as Spring Security configuration class.
// Activates default Security
// Enable Custom defined Configuration from this class
@EnableMethodSecurity
// Enable Method Level Security
public class SecurityConfig  {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> {
                    requests
//                            .requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers("/admin/**").hasAnyRole(String.valueOf(MUserRoles.ADMIN))
                            .requestMatchers("/user/create", "/api/v1/loan", "/h2-console/**", "/api/v1/bank-rates").permitAll()
                            .anyRequest().authenticated();
        });

        //  by making stateless we don't have to remember user state,
        //  each request will be verified independently.
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // CSRF Protection is enabled by default in Spring Security.
        // We need to disable it for our REST API as we are not using cookies for session management.
        // Also, our session management is stateless.
        http.csrf((csrf) -> csrf.disable());
        http.headers((headers) -> headers.defaultsDisabled() // Disable default headers
                .frameOptions((frameOptions) -> frameOptions.sameOrigin())); // Allow iframes for same-origin requests
        http.httpBasic(withDefaults());
        return http.build();
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
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        //  ONE WAY ->
//        //  return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/images/**");
//
//        //  ANOTHER WAY ->
//        WebSecurityCustomizer webSecurityCustomizer = new WebSecurityCustomizer() {
//            @Override
//            public void customize(WebSecurity web) {
//                web.ignoring().requestMatchers("/css/**", "/js/**", "/images/**");
//            }
//        };
//        return webSecurityCustomizer;
//    }


}
