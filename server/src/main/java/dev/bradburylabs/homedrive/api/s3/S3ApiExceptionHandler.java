package dev.bradburylabs.homedrive.api.s3;

import static dev.bradburylabs.homedrive.util.S3Constants.REQUEST_ID;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_REQUEST_ID_HEADER;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import dev.bradburylabs.homedrive.api.s3.exception.BucketNotFoundException;
import dev.bradburylabs.homedrive.api.s3.exception.InvalidRequestException;
import dev.bradburylabs.homedrive.api.s3.exception.ObjectNotModifiedException;
import dev.bradburylabs.homedrive.api.s3.exception.ObjectPreconditionFailedException;
import dev.bradburylabs.homedrive.exception.BadDigestException;
import dev.bradburylabs.homedrive.exception.InvalidObjectUploadPartException;
import dev.bradburylabs.homedrive.exception.InvalidObjectUploadPartsOrderException;
import dev.bradburylabs.homedrive.exception.ObjectUploadNotFoundException;
import dev.bradburylabs.homedrive.exception.UserObjectNotFoundException;
import dev.bradburylabs.homedrive.model.s3.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice(basePackages = "dev.bradburylabs.homedrive.api.s3")
@Slf4j
public class S3ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Object> handleAuthorizationDeniedException(AuthorizationDeniedException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.FORBIDDEN, "AccessDenied", "Access Denied", request);
    }

    @ExceptionHandler(ObjectPreconditionFailedException.class)
    public ResponseEntity<Object> handleObjectPreconditionFailedException(ObjectPreconditionFailedException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.PRECONDITION_FAILED, "PreconditionFailed", "At least one of the preconditions that you specified did not hold.",
                request);
    }

    @ExceptionHandler(ObjectNotModifiedException.class)
    public ResponseEntity<Object> handleObjectNotModifiedException(ObjectNotModifiedException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.NOT_MODIFIED, "NotModified", "The resource was not changed.", request);
    }

    @ExceptionHandler(BadDigestException.class)
    public ResponseEntity<Object> handleBadDigestException(BadDigestException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.BAD_REQUEST, "BadDigest",
                "The Content-MD5 or checksum value that you specified did not match what the server received.", request);
    }

    @ExceptionHandler(BucketNotFoundException.class)
    public ResponseEntity<Object> handleBucketNotFoundException(BucketNotFoundException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.NOT_FOUND, "NoSuchBucket", "The specified bucket does not exist.", request);
    }

    @ExceptionHandler(UserObjectNotFoundException.class)
    public ResponseEntity<Object> handleUserObjectNotFoundException(UserObjectNotFoundException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.NOT_FOUND, "NoSuchKey", "The specified key does not exist.", request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Object> handleInvalidRequestException(InvalidRequestException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.BAD_REQUEST, "InvalidRequest", e.getMessage(), request);
    }

    @ExceptionHandler(ObjectUploadNotFoundException.class)
    public ResponseEntity<Object> handleObjectUploadNotFoundException(ObjectUploadNotFoundException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.NOT_FOUND, "NoSuchUpload",
                "The specified multipart upload does not exist. The upload ID might be invalid, or the multipart upload might have been aborted or completed.",
                request);
    }

    @ExceptionHandler(InvalidObjectUploadPartsOrderException.class)
    public ResponseEntity<Object> handleInvalidObjectUploadPartsOrderException(InvalidObjectUploadPartsOrderException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.BAD_REQUEST, "InvalidPartOrder",
                "The list of parts was not in ascending order. The parts list must be specified in order by part number.", request);
    }

    @ExceptionHandler(InvalidObjectUploadPartException.class)
    public ResponseEntity<Object> handleInvalidObjectUploadPartException(InvalidObjectUploadPartException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.BAD_REQUEST, "InvalidPart",
                "One or more of the specified parts could not be found. The part might not have been uploaded, or the specified ETag might not have matched the uploaded part's ETag.",
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.INTERNAL_SERVER_ERROR, "InternalError", "An internal error occurred. Try again.", request);
    }

    protected ResponseEntity<Object> handleS3Exception(Exception e, HttpStatus status, String code, String message, WebRequest request) {
        log.error(e.getMessage(), e);

        String resource = "";
        String requestId = MDC.get(REQUEST_ID);

        if (request instanceof ServletWebRequest servletWebRequest) {
            resource = servletWebRequest.getRequest().getRequestURI();

            try {
                while (servletWebRequest.getRequest().getInputStream().skip(Long.MAX_VALUE) == Long.MAX_VALUE) {
                    // Read and discard bytes
                }
            } catch (IOException _) {
                // Ignore
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(code, message, resource, requestId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        headers.add(X_AMZ_REQUEST_ID_HEADER, requestId);

        return handleExceptionInternal(e, errorResponse, headers, status, request);
    }
}
