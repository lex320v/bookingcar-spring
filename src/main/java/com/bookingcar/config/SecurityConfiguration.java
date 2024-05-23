package com.bookingcar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static com.bookingcar.entity.enums.Role.ADMIN;
import static com.bookingcar.entity.enums.Role.CLIENT;
import static com.bookingcar.entity.enums.Role.OWNER;
import static com.bookingcar.entity.enums.Role.SUPER_ADMIN;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(urlConfig -> urlConfig
                        .requestMatchers(
                                "/",
                                "/login",
                                "/users/registration",
                                "/users/sign-up",
                                "/swagger-ui/**",
                                "/css/**",
                                "/js/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(POST, "/admins").hasAuthority(SUPER_ADMIN.getAuthority())
                        .requestMatchers("/admins/**").hasAnyAuthority(SUPER_ADMIN.getAuthority(), ADMIN.getAuthority())
                        .requestMatchers(GET, "/cars", "/cars/{id:\\d+}").authenticated()
                        .requestMatchers("/cars/**").hasAuthority(OWNER.getAuthority())
                        .requestMatchers(POST, "/requests").hasAuthority(CLIENT.getAuthority())
                        .requestMatchers(GET, "/requests").hasAnyAuthority(OWNER.getAuthority(), CLIENT.getAuthority())
                        .requestMatchers(POST, "/feedbacks").hasAuthority(CLIENT.getAuthority())
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/users/profile")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
