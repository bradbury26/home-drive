package dev.bradburylabs.homedrive.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ConfigurationProperties(prefix = "home-drive")
@NoArgsConstructor
@Getter
@Setter
public class HomeDriveProperties {
    private SecurityProperties security;

    private String dataLocation;


    @NoArgsConstructor
    @Getter
    @Setter
    public static class SecurityProperties {
        /**
         * Key for creating persistent login tokens
         */
        private String tokenKey;
    }
}
