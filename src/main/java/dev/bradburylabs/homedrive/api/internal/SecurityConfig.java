package dev.bradburylabs.homedrive.api.internal;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import dev.bradburylabs.homedrive.api.NoopAuthenticationSuccessHandler;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final HomeDriveProperties homeDriveProperties;

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
            RememberMeAuthenticationProvider rememberMeAuthenticationProvider) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider, rememberMeAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();

        encoders.put("argon2@SpringSecurity_v5_8", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());

        return new DelegatingPasswordEncoder("argon2@SpringSecurity_v5_8", encoders);
    }

    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager,
            RememberMeServices rememberMeServices) {
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter(authenticationManager);
        filter.setAuthenticationSuccessHandler(new NoopAuthenticationSuccessHandler());
        filter.setRememberMeServices(rememberMeServices);

        return filter;
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl persistentTokenRepository = new JdbcTokenRepositoryImpl();

        persistentTokenRepository.setDataSource(dataSource);

        return persistentTokenRepository;
    }

    @Bean
    public PersistentTokenBasedRememberMeServices rememberMeServices(UserDetailsService userDetailsService,
            PersistentTokenRepository persistentTokenRepository) {
        PersistentTokenBasedRememberMeServices tokenBasedRememberMeServices =
                new PersistentTokenBasedRememberMeServices(homeDriveProperties.getSecurity().getTokenKey(), userDetailsService, persistentTokenRepository);
        tokenBasedRememberMeServices.setAlwaysRemember(true);

        return tokenBasedRememberMeServices;
    }

    @Bean
    public RememberMeAuthenticationFilter rememberMeFilter(AuthenticationManager authenticationManager, RememberMeServices rememberMeServices) {
        return new RememberMeAuthenticationFilter(authenticationManager, rememberMeServices);
    }

    @Bean
    public RememberMeAuthenticationProvider rememberMeAuthenticationProvider() {
        return new RememberMeAuthenticationProvider(homeDriveProperties.getSecurity().getTokenKey());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter,
            RememberMeAuthenticationFilter rememberMeAuthenticationFilter, AbstractRememberMeServices rememberMeServices) {
        http.securityMatcher("/api/**").formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
                .addFilterAfter(usernamePasswordAuthenticationFilter, LogoutFilter.class)
                .addFilterAfter(rememberMeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(t -> t.requestMatchers("/error").permitAll().anyRequest().rememberMe()).csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(t -> t.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .logout(t -> t.logoutSuccessUrl("/").addLogoutHandler(rememberMeServices));

        return http.build();
    }
}
