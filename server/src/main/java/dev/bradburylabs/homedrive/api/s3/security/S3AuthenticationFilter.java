package dev.bradburylabs.homedrive.api.s3.security;

import static dev.bradburylabs.homedrive.util.S3Constants.REQUEST_ID;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CONTENT_SHA256_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_DATE_HEADER;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;
import dev.bradburylabs.homedrive.api.s3.exception.S3AuthenticationException;
import dev.bradburylabs.homedrive.api.s3.signature.AuthorizationHeaderSignatureValidator;
import dev.bradburylabs.homedrive.entity.AppUser;
import dev.bradburylabs.homedrive.entity.S3ApiToken;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import dev.bradburylabs.homedrive.repository.UserRepository;
import dev.bradburylabs.homedrive.util.DateUtils;
import dev.bradburylabs.homedrive.util.EncryptionUtils;
import dev.bradburylabs.homedrive.util.IdUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class S3AuthenticationFilter extends GenericFilterBean {
    private static final Logger log = LoggerFactory.getLogger(S3AuthenticationFilter.class);

    private static final Pattern AUTHORIZATION_PATTERN =
            Pattern.compile("AWS4-HMAC-SHA256\\sCredential=(.*)/\\d{8}/(.*)/s3/aws4_request,\\s?SignedHeaders=(.*),\\s?Signature=(.*)");
    private static final RequestMatcher S3_REQUEST_MATCHER = new S3RequestMatcher();

    private final UserRepository userRepository;
    private final HomeDriveProperties homeDriveProperties;

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();
    private final AuthenticationFailureHandler authenticationFailureHandler = new S3AuthenticationFailureHandler();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!S3_REQUEST_MATCHER.matches(request) || "/error".equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        MDC.put(REQUEST_ID, IdUtils.generateId());

        try {
            Authentication authentication = attemptAuthentication(request);

            successfulAuthentication(request, response, authentication);
        } catch (AuthenticationException e) {
            unsuccessfulAuthentication(request, response, e);
            return;
        }

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            throw e;
        } finally {
            MDC.remove(REQUEST_ID);
        }
    }

    protected Authentication attemptAuthentication(HttpServletRequest request) throws IOException, ServletException {
        S3AuthenticationToken s3AuthenticationToken = createTokenFromAuthorizationHeader(request);

        return validateToken(request, s3AuthenticationToken);
    }

    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) {
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authResult);
        this.securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        this.securityContextHolderStrategy.clearContext();
        this.authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
    }

    private S3AuthenticationToken createTokenFromAuthorizationHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.isBlank(authorizationHeader)) {
            throw new S3AuthenticationException("Your request is missing a required header.", HttpStatus.BAD_REQUEST, "MissingSecurityHeader");
        }

        Matcher matcher = AUTHORIZATION_PATTERN.matcher(authorizationHeader);

        if (!matcher.matches()) {
            throw new S3AuthenticationException("The authorization header that you provided is not valid.", HttpStatus.BAD_REQUEST,
                    "AuthorizationHeaderMalformed");
        }

        String accessKeyId = matcher.group(1);
        String region = matcher.group(2);
        String signedHeaders = matcher.group(3);
        String signature = matcher.group(4);

        Instant signingInstant = getSigningInstant(request);

        if (signingInstant == null) {
            throw new S3AuthenticationException("The request was missing a required header.", HttpStatus.BAD_REQUEST, "InvalidArgument");
        }

        S3AuthenticationDetails authenticationDetails = new S3AuthenticationDetails(region, signedHeaders, signature, signingInstant);

        return new S3AuthenticationToken(accessKeyId, authenticationDetails);
    }

    private Instant getSigningInstant(HttpServletRequest request) {
        String dateHeader = request.getHeader(X_AMZ_DATE_HEADER);

        if (StringUtils.isBlank(dateHeader)) {
            return null;
        }

        try {
            return DateUtils.DATE_TIME_FORMAT.parse(dateHeader, Instant::from);
        } catch (DateTimeParseException e) {
            log.error("Invalid date header", e);
            return null;
        }
    }

    private Authentication validateToken(HttpServletRequest request, S3AuthenticationToken s3AuthenticationToken) {
        String accessKeyId = s3AuthenticationToken.getName();

        Optional<S3ApiToken> s3ApiTokenOptional = userRepository.findS3ApiTokenByAccessKeyId(accessKeyId);

        if (s3ApiTokenOptional.isEmpty()) {
            throw new S3AuthenticationException("The AWS access key ID that you provided does not exist in our records.", HttpStatus.FORBIDDEN,
                    "InvalidAccessKeyId");
        }

        S3ApiToken s3ApiToken = s3ApiTokenOptional.get();
        AppUser user = s3ApiToken.getUser();
        String secretAccessKey = EncryptionUtils.decrypt(homeDriveProperties.getSecurity().getTokenKey(), s3ApiToken.getSecretAccessKey());

        S3AuthenticationDetails s3AuthenticationDetails = (S3AuthenticationDetails) s3AuthenticationToken.getDetails();

        if (s3AuthenticationDetails == null) {
            throw new IllegalArgumentException("S3AuthenticationDetails cannot be null.");
        }

        AuthorizationHeaderSignatureValidator signatureValidator = new AuthorizationHeaderSignatureValidator(s3AuthenticationDetails, secretAccessKey);

        String sha256Content = request.getHeader(X_AMZ_CONTENT_SHA256_HEADER);

        if (!signatureValidator.validateSignature(request, sha256Content)) {
            throw new S3AuthenticationException("The request signature that the server calculated does not match the signature that you provided.",
                    HttpStatus.BAD_REQUEST, "InvalidSignature");
        }

        AbstractAuthenticationToken authentication = createAuthentication(user, secretAccessKey, s3ApiToken);
        authentication.setDetails(s3AuthenticationDetails);

        return authentication;
    }

    private AbstractAuthenticationToken createAuthentication(AppUser user, String secretAccessKey, S3ApiToken s3ApiToken) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        if (s3ApiToken.isReadPermission()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_READ"));
        }

        if (s3ApiToken.isWritePermission()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_WRITE"));
        }

        if (user.isAdminUser()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new UsernamePasswordAuthenticationToken(user.getUsername(), secretAccessKey, grantedAuthorities);
    }
}
