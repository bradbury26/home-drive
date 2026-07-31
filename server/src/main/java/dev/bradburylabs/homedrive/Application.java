package dev.bradburylabs.homedrive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EntityScan
@EnableJpaRepositories
@EnableMethodSecurity
@EnableAsync
@EnableResilientMethods
@EnableScheduling
@ConfigurationPropertiesScan
public class Application {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
