package dev.bradburylabs.homedrive.api.s3.security;

import static dev.bradburylabs.homedrive.util.S3Constants.REQUEST_ID;
import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_REQUEST_ID_HEADER;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.JacksonXmlHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import dev.bradburylabs.homedrive.api.s3.exception.S3AuthenticationException;
import dev.bradburylabs.homedrive.model.s3.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class S3AuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final HttpMessageConverter<Object> converter = new JacksonXmlHttpMessageConverter();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        while (request.getInputStream().skip(Long.MAX_VALUE) == Long.MAX_VALUE) {
            // Read and discard bytes
        }

        String requestId = MDC.get(REQUEST_ID);

        ErrorResponse errorResponse;
        if (exception instanceof S3AuthenticationException s3AuthenticationException) {
            errorResponse = new ErrorResponse(s3AuthenticationException.getCode(), s3AuthenticationException.getMessage(), request.getRequestURI(), requestId);

            response.setStatus(s3AuthenticationException.getStatus().value());
        } else {
            errorResponse = new ErrorResponse("InternalError", "An internal error occurred. Try again.", request.getRequestURI(), requestId);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.setHeader(X_AMZ_REQUEST_ID_HEADER, requestId);

        converter.write(errorResponse, MediaType.APPLICATION_XML, new ServletServerHttpResponse(response));
    }
}
