package dev.bradburylabs.homedrive.api.internal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final PersistentTokenRefresher persistentTokenRefresher;

    @PostMapping("/token/refresh")
    public void tokenRefresh(HttpServletRequest request, HttpServletResponse response) {
        persistentTokenRefresher.refreshToken(request, response);
    }
}
