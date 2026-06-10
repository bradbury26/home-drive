package dev.bradburylabs.homedrive.api.internal;

import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import dev.bradburylabs.homedrive.api.s3.exception.InvalidSignatureException;
import dev.bradburylabs.homedrive.model.s3.ErrorResponse;

@ControllerAdvice(basePackages = "dev.bradburylabs.homedrive.api.internal")
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(InvalidSignatureException.class)
    public ResponseEntity<Object> handleInvalidSignatureException(InvalidSignatureException e, WebRequest request) {
        return handleS3Exception(e, HttpStatus.FORBIDDEN, "AccessDenied", "Access Denied", request);
    }

    protected ResponseEntity<Object> handleS3Exception(Exception e, HttpStatus status, String code, String message, WebRequest request) {
        String resource = "";
        String requestId = UUID.randomUUID().toString();

        if (request instanceof ServletWebRequest servletWebRequest) {
            resource = servletWebRequest.getRequest().getRequestURL().toString();
        }

        ErrorResponse errorResponse = new ErrorResponse(code, message, requestId, resource);
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-amz-request-id", requestId);

        return handleExceptionInternal(e, errorResponse, headers, status, request);
    }
}
