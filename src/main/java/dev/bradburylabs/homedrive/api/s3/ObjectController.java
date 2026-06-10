package dev.bradburylabs.homedrive.api.s3;

import static dev.bradburylabs.homedrive.util.DateUtils.HEADER_DATE_TIME_FORMAT;
import static dev.bradburylabs.homedrive.util.S3Constants.CHECKSUM_HEADERS;
import static dev.bradburylabs.homedrive.util.S3Constants.CONTENT_MD5_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.CONTENT_TYPE_AWS_CHUNKED;
import static dev.bradburylabs.homedrive.util.S3Constants.REQUEST_ID;
import static dev.bradburylabs.homedrive.util.S3Constants.RESPONSE_CONTENT_ENCODING_PARAMETER;
import static dev.bradburylabs.homedrive.util.S3Constants.RESPONSE_CONTENT_TYPE_PARAMETER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CHECKSUM_CRC32_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CHECKSUM_MD5_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CHECKSUM_SHA1_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CHECKSUM_SHA256_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CHECKSUM_SHA512_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CONTENT_SHA256_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_REQUEST_ID_HEADER;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_TRAILER_HEADER;
import static org.springframework.http.HttpHeaders.CONTENT_ENCODING;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;
import static org.springframework.http.HttpHeaders.IF_UNMODIFIED_SINCE;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.springframework.http.HttpHeaders.RANGE;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import dev.bradburylabs.homedrive.api.annotation.ReadAccess;
import dev.bradburylabs.homedrive.api.annotation.WriteAccess;
import dev.bradburylabs.homedrive.api.s3.exception.BucketNotFoundException;
import dev.bradburylabs.homedrive.api.s3.exception.ObjectPreconditionFailedException;
import dev.bradburylabs.homedrive.exception.UserNotFoundException;
import dev.bradburylabs.homedrive.exception.UserObjectNotFoundException;
import dev.bradburylabs.homedrive.model.object.Checksum;
import dev.bradburylabs.homedrive.model.object.HttpRange;
import dev.bradburylabs.homedrive.model.object.RetrieveObjectResponse;
import dev.bradburylabs.homedrive.model.object.StoreObjectResponse;
import dev.bradburylabs.homedrive.model.s3.DeletedObject;
import dev.bradburylabs.homedrive.model.s3.EncodingType;
import dev.bradburylabs.homedrive.model.s3.Error;
import dev.bradburylabs.homedrive.model.s3.ListObjectV2Request;
import dev.bradburylabs.homedrive.model.s3.ListObjectsRequest;
import dev.bradburylabs.homedrive.model.s3.ListObjectsResult;
import dev.bradburylabs.homedrive.model.s3.ListObjectsV2Result;
import dev.bradburylabs.homedrive.model.s3.ObjectIdentifier;
import dev.bradburylabs.homedrive.model.s3.S3DeleteObjectRequest;
import dev.bradburylabs.homedrive.model.s3.S3DeleteObjectsRequest;
import dev.bradburylabs.homedrive.model.s3.S3DeleteObjectsResponse;
import dev.bradburylabs.homedrive.model.s3.S3RetrieveObjectRequest;
import dev.bradburylabs.homedrive.model.s3.S3StoreObjectRequest;
import dev.bradburylabs.homedrive.service.UserService;
import dev.bradburylabs.homedrive.service.s3.S3ListObjectsService;
import dev.bradburylabs.homedrive.service.s3.S3ObjectRetrievalService;
import dev.bradburylabs.homedrive.service.s3.S3ObjectStorageService;
import dev.bradburylabs.homedrive.util.ChecksumType;
import dev.bradburylabs.homedrive.util.RangeHeaderParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(headers = X_AMZ_CONTENT_SHA256_HEADER)
@RequiredArgsConstructor
@Slf4j
public class ObjectController {
    private final S3ObjectStorageService s3ObjectStorageService;
    private final S3ObjectRetrievalService s3ObjectRetrievalService;
    private final S3ListObjectsService s3ListObjectsService;
    private final UserService userService;

    @RequestMapping(value = "/{bucketName}/{*path}", method = RequestMethod.HEAD)
    @ReadAccess
    public ResponseEntity<Void> headObject(@PathVariable String bucketName, @PathVariable String path,
            @RequestHeader(value = IF_MATCH, required = false) String ifMatch, @RequestHeader(value = IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = IF_MODIFIED_SINCE, required = false) Instant ifModifiedSince,
            @RequestHeader(value = IF_UNMODIFIED_SINCE, required = false) Instant ifUnmodifiedSince,
            @RequestHeader(value = RANGE, required = false) String rangeHeader,
            @RequestParam(value = RESPONSE_CONTENT_ENCODING_PARAMETER, required = false) String responseContentEncoding,
            @RequestParam(value = RESPONSE_CONTENT_TYPE_PARAMETER, required = false) String responseContentType) {
        return retrieveObject(bucketName, path, ifMatch, ifNoneMatch, ifModifiedSince, ifUnmodifiedSince, rangeHeader, responseContentEncoding,
                responseContentType, false);
    }

    @GetMapping("/{bucketName}/{*path}")
    @ReadAccess
    public ResponseEntity<StreamingResponseBody> getObject(@PathVariable String bucketName, @PathVariable String path,
            @RequestHeader(value = IF_MATCH, required = false) String ifMatch, @RequestHeader(value = IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = IF_MODIFIED_SINCE, required = false) Instant ifModifiedSince,
            @RequestHeader(value = IF_UNMODIFIED_SINCE, required = false) Instant ifUnmodifiedSince,
            @RequestHeader(value = RANGE, required = false) String rangeHeader,
            @RequestParam(value = RESPONSE_CONTENT_ENCODING_PARAMETER, required = false) String responseContentEncoding,
            @RequestParam(value = RESPONSE_CONTENT_TYPE_PARAMETER, required = false) String responseContentType) {
        return retrieveObject(bucketName, path, ifMatch, ifNoneMatch, ifModifiedSince, ifUnmodifiedSince, rangeHeader, responseContentEncoding,
                responseContentType, true);
    }

    @GetMapping(value = {"/{bucketName}", "/{bucketName}/"}, produces = MediaType.APPLICATION_XML_VALUE)
    @ReadAccess
    public ListObjectsResult listObjects(@PathVariable String bucketName, @RequestParam(value = "delimiter", required = false) String delimiter,
            @RequestParam(value = "prefix", required = false) String prefix, @RequestParam(value = "marker", required = false) String marker,
            @RequestParam(value = "max-keys", required = false) Integer maxKeys, @RequestParam(value = "encoding-type", required = false) String encodingType) {
        checkBucketAccess(bucketName);

        return s3ListObjectsService.listObjects(userId(bucketName),
                new ListObjectsRequest(bucketName, delimiter, prefix, marker, maxKeys, EncodingType.fromName(encodingType)));
    }

    @GetMapping(value = "/{bucketName}", params = "list-type=2", produces = MediaType.APPLICATION_XML_VALUE)
    @ReadAccess
    public ListObjectsV2Result listObjectsV2(@PathVariable String bucketName, @RequestParam(value = "delimiter", required = false) String delimiter,
            @RequestParam(value = "prefix", required = false) String prefix,
            @RequestParam(value = "continuation-token", required = false) String continuationToken,
            @RequestParam(value = "max-keys", required = false) Integer maxKeys, @RequestParam(value = "start-after", required = false) String startAfter,
            @RequestParam(value = "encoding-type", required = false) String encodingType) {
        checkBucketAccess(bucketName);

        return s3ListObjectsService.listObjectsV2(userId(bucketName),
                new ListObjectV2Request(bucketName, delimiter, prefix, continuationToken, maxKeys, startAfter, EncodingType.fromName(encodingType)));
    }

    @PutMapping("/{bucketName}/{*path}")
    @WriteAccess
    public ResponseEntity<Void> putObject(@PathVariable String bucketName, @PathVariable String path,
            @RequestHeader(X_AMZ_CONTENT_SHA256_HEADER) String contentSha256, @RequestHeader(value = CONTENT_ENCODING, required = false) String contentEncoding,
            @RequestHeader(value = CONTENT_TYPE, defaultValue = MediaType.APPLICATION_OCTET_STREAM_VALUE) String contentType,
            @RequestHeader(value = IF_MATCH, required = false) String ifMatch, @RequestHeader(value = IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = CONTENT_MD5_HEADER, required = false) String contentMd5,
            @RequestHeader(value = X_AMZ_TRAILER_HEADER, required = false) String trailerHeader, InputStream inputStream, HttpServletRequest request) {
        checkBucketAccess(bucketName);

        S3StoreObjectRequest objectStorageRequest =
                new S3StoreObjectRequest(userId(bucketName), path.substring(1), sanitiseContentEncoding(contentEncoding), contentType,
                        checksum(request, contentMd5, trailerHeader), ifMatch, "*".equals(ifNoneMatch), trailerHeader);

        StoreObjectResponse result = switch (contentSha256) {
            case "STREAMING-AWS4-HMAC-SHA256-PAYLOAD-TRAILER" -> s3ObjectStorageService.storeObjectStream(objectStorageRequest, inputStream, true);
            case "STREAMING-AWS4-HMAC-SHA256-PAYLOAD" -> s3ObjectStorageService.storeObjectStream(objectStorageRequest, inputStream, false);
            default -> s3ObjectStorageService.storeObject(objectStorageRequest, inputStream);
        };

        HttpHeaders headers = new HttpHeaders();
        headers.add(X_AMZ_REQUEST_ID_HEADER, MDC.get(REQUEST_ID));
        headers.add(ETAG, result.etag());
        addChecksumHeader(headers, result.checksum());

        return ResponseEntity.ok().headers(headers).build();
    }

    @DeleteMapping("/{bucketName}/{*path}")
    @WriteAccess
    public ResponseEntity<Void> deleteObject(@PathVariable String bucketName, @PathVariable String path,
            @RequestHeader(value = IF_MATCH, required = false) String ifMatch) {
        checkBucketAccess(bucketName);

        S3DeleteObjectRequest deleteRequest = new S3DeleteObjectRequest(userId(bucketName), path.substring(1), ifMatch);
        s3ObjectStorageService.deleteObject(deleteRequest);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{bucketName}", params = "delete", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    @WriteAccess
    public S3DeleteObjectsResponse deleteObjects(@PathVariable String bucketName, @RequestBody S3DeleteObjectsRequest request) {
        checkBucketAccess(bucketName);

        String userId = userId(bucketName);

        List<DeletedObject> deletedObjects = new ArrayList<>();
        List<Error> errors = new ArrayList<>();

        for (ObjectIdentifier objectIdentifier : request.objectIdentifiers()) {
            try {
                s3ObjectStorageService.deleteObject(new S3DeleteObjectRequest(userId, objectIdentifier.key(), objectIdentifier.etag()));
                deletedObjects.add(new DeletedObject(objectIdentifier.key()));
            } catch (UserObjectNotFoundException e) {
                errors.add(new Error("NoSuchKey", objectIdentifier.key(), "The specified key does not exist."));
            } catch (ObjectPreconditionFailedException e) {
                errors.add(new Error("PreconditionFailed", objectIdentifier.key(), "At least one of the preconditions that you specified did not hold."));
            } catch (Exception e) {
                errors.add(new Error("InternalError", objectIdentifier.key(), "An internal error occurred. Try again."));
            }
        }

        return new S3DeleteObjectsResponse(deletedObjects, errors);
    }

    public <T> ResponseEntity<T> retrieveObject(String bucketName, String path, String ifMatch, String ifNoneMatch, Instant ifModifiedSince,
            Instant ifUnmodifiedSince, String rangeHeader, String responseContentEncoding, String responseContentType, boolean retrieveInputStream) {
        checkBucketAccess(bucketName);

        HttpRange range = RangeHeaderParser.parse(rangeHeader);
        S3RetrieveObjectRequest objectRetrievalRequest =
                new S3RetrieveObjectRequest(userId(bucketName), path.substring(1), range, ifMatch, ifNoneMatch, ifModifiedSince, ifUnmodifiedSince);
        RetrieveObjectResponse objectRetrievalResponse = s3ObjectRetrievalService.retrieveObject(objectRetrievalRequest, retrieveInputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add(X_AMZ_REQUEST_ID_HEADER, MDC.get(REQUEST_ID));
        headers.add(ETAG, objectRetrievalResponse.etag());
        headers.add(LAST_MODIFIED, HEADER_DATE_TIME_FORMAT.format(objectRetrievalResponse.lastModified()));
        headers.add(CONTENT_ENCODING, Optional.ofNullable(responseContentEncoding).orElse(objectRetrievalResponse.contentEncoding()));
        headers.add(CONTENT_TYPE, Optional.ofNullable(responseContentType).orElse(objectRetrievalResponse.contentType()));
        headers.add(CONTENT_LENGTH, Optional.ofNullable(range).map(item -> String.valueOf(item.totalBytes(objectRetrievalResponse.contentLength())))
                .orElse(String.valueOf(objectRetrievalResponse.contentLength())));
        addChecksumHeader(headers, objectRetrievalResponse.checksum());

        if (retrieveInputStream) {
            StreamingResponseBody responseBody = outputStream -> {
                try (InputStream inputStream = objectRetrievalResponse.inputStream()) {
                    inputStream.transferTo(outputStream);
                }
            };

            return (ResponseEntity<T>) ResponseEntity.ok().headers(headers).body(responseBody);
        } else {
            return ResponseEntity.ok().headers(headers).build();
        }
    }

    private void checkBucketAccess(String bucketName) {
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

    private String userId(String bucketName) {
        try {
            return userService.readUserByUsername(bucketName).id();
        } catch (UserNotFoundException e) {
            throw new BucketNotFoundException();
        }
    }

    private String sanitiseContentEncoding(String contentEncoding) {
        if (contentEncoding == null) {
            return null;
        }

        String sanitisedContentEncoding =
                Stream.of(contentEncoding.split(",")).filter(item -> !CONTENT_TYPE_AWS_CHUNKED.equals(item)).collect(Collectors.joining(","));

        return sanitisedContentEncoding.isBlank() ? null : sanitisedContentEncoding;
    }

    private Checksum checksum(HttpServletRequest request, String contentMd5, String trailerHeader) {
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

    private void addChecksumHeader(HttpHeaders headers, Checksum checksum) {
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
