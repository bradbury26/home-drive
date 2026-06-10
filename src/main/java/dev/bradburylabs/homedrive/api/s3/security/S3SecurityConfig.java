package dev.bradburylabs.homedrive.api.s3.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import dev.bradburylabs.homedrive.repository.UserRepository;

@Configuration
public class S3SecurityConfig {
    @Bean
    public S3AuthenticationFilter s3AuthenticationFilter(UserRepository userRepository, HomeDriveProperties homeDriveProperties) {
        return new S3AuthenticationFilter(userRepository, homeDriveProperties);
    }

    @Bean
    public SecurityFilterChain s3SecurityFilterChain(HttpSecurity http, S3AuthenticationFilter s3AuthenticationFilter) {
        http.securityMatcher(new S3RequestMatcher()).formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
                .addFilterAfter(s3AuthenticationFilter, LogoutFilter.class).authorizeHttpRequests(t -> t.anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable).sessionManagement(t -> t.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .logout(AbstractHttpConfigurer::disable).headers(t -> t.cacheControl(HeadersConfigurer.CacheControlConfig::disable));

        return http.build();
    }
}
