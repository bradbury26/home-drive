package dev.bradburylabs.homedrive.api.s3.security;

import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class S3AuthenticationToken extends AbstractAuthenticationToken {
    private final String accessKeyId;

    public S3AuthenticationToken(String accessKeyId, S3AuthenticationDetails details) {
        super((Collection<? extends GrantedAuthority>) null);

        this.accessKeyId = accessKeyId;
        setDetails(details);
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return accessKeyId;
    }
}
