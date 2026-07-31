package dev.bradburylabs.homedrive.api.internal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface PersistentTokenRefresher {
    void refreshToken(HttpServletRequest request, HttpServletResponse response);
}
