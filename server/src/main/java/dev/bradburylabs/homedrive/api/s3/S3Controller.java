package dev.bradburylabs.homedrive.api.s3;

import static dev.bradburylabs.homedrive.util.S3Constants.CHECKSUM_HEADERS;
import static dev.bradburylabs.homedrive.util.S3Constants.CONTENT_TYPE_AWS_CHUNKED;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CHECKSUM_CRC32_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CHECKSUM_MD5_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CHECKSUM_SHA1_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CHECKSUM_SHA256_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CHECKSUM_SHA512_HEADER;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import dev.bradburylabs.homedrive.api.s3.exception.BucketNotFoundException;
import dev.bradburylabs.homedrive.exception.UserNotFoundException;
import dev.bradburylabs.homedrive.model.object.Checksum;
import dev.bradburylabs.homedrive.service.UserService;
import dev.bradburylabs.homedrive.util.ChecksumType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class S3Controller {
    protected final UserService userService;

    protected void checkBucketAccess(String bucketName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalArgumentException("Authentication is null");
        }

        boolean adminRole =
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(authority -> Objects.equals(authority, "ROLE_ADMIN"));

        if (!bucketName.equals(authentication.getName()) && !adminRole) {
            throw new AuthorizationDeniedException("User doesn't have permission to access this bucket");
        }
    }

    protected String userId(String bucketName) {
        try {
            return userService.readUserByUsername(bucketName).id();
        } catch (UserNotFoundException e) {
            throw new BucketNotFoundException();
        }
    }

    protected String sanitiseContentEncoding(String contentEncoding) {
        if (contentEncoding == null) {
            return null;
        }

        String sanitisedContentEncoding =
                Stream.of(contentEncoding.split(",")).filter(item -> !CONTENT_TYPE_AWS_CHUNKED.equals(item)).collect(Collectors.joining(","));

        return sanitisedContentEncoding.isBlank() ? null : sanitisedContentEncoding;
    }

    protected Checksum checksum(HttpServletRequest request, String contentMd5, String trailerHeader) {
        for (String checksumHeader : CHECKSUM_HEADERS) {
            String headerValue = request.getHeader(checksumHeader);

            if (headerValue != null) {
                ChecksumType checksumType = ChecksumType.valueOf(checksumHeader.substring(checksumHeader.lastIndexOf("-") + 1).toUpperCase());

                return new Checksum(checksumType, headerValue);
            }
        }

        if (contentMd5 != null) {
            return new Checksum(ChecksumType.MD5, contentMd5);
        }

        if (trailerHeader != null) {
            return new Checksum(ChecksumType.valueOf(trailerHeader.substring(trailerHeader.lastIndexOf("-") + 1).toUpperCase()), null);
        }

        return Checksum.empty();
    }

    protected void addChecksumHeader(HttpHeaders headers, Checksum checksum) {
        if (checksum.checksumType() != null) {
            String headerName = switch (checksum.checksumType()) {
                case CRC32 -> X_AMZ_CHECKSUM_CRC32_HEADER;
                case MD5 -> X_AMZ_CHECKSUM_MD5_HEADER;
                case SHA1 -> X_AMZ_CHECKSUM_SHA1_HEADER;
                case SHA256 -> X_AMZ_CHECKSUM_SHA256_HEADER;
                case SHA512 -> X_AMZ_CHECKSUM_SHA512_HEADER;
            };

            headers.add(headerName, checksum.checksum());
        }
    }
}
