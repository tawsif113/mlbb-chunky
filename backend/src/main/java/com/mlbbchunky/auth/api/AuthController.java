package com.mlbbchunky.auth.api;

import com.mlbbchunky.auth.application.AuthService;
import com.mlbbchunky.auth.application.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth/mlbb")
public class AuthController {
    private static final String SESSION_COOKIE = "chunky_session";

    private final AuthService authService;
    private final boolean secureCookie;

    public AuthController(
            AuthService authService,
            @Value("${app.auth.secure-cookie:false}") boolean secureCookie
    ) {
        this.authService = authService;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/verification-code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendVerificationCode(@Valid @RequestBody VerificationCodeRequest request) {
        authService.sendVerificationCode(request.roleId(), request.zoneId());
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthenticatedUser> verify(
            @Valid @RequestBody VerifyRequest request,
            @CookieValue(name = SESSION_COOKIE, required = false) String existingSessionToken
    ) {
        AuthService.Session session = authService.verify(
                request.roleId(),
                request.zoneId(),
                request.verificationCode()
        );

        // Verification succeeded and a replacement session now exists, so any previous
        // browser session can be revoked without locking the user out if verification fails.
        authService.logout(existingSessionToken);

        Duration maxAge = Duration.between(Instant.now(), session.expiresAt());
        ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE, session.token())
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(session.user());
    }

    @GetMapping("/me")
    public AuthenticatedUser me(
            @CookieValue(name = SESSION_COOKIE, required = false) String sessionToken
    ) {
        return authService.currentUser(sessionToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = SESSION_COOKIE, required = false) String sessionToken
    ) {
        authService.logout(sessionToken);

        ResponseCookie expiredCookie = ResponseCookie.from(SESSION_COOKIE, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }

    public record VerificationCodeRequest(@Positive long roleId, @Positive long zoneId) {}

    public record VerifyRequest(
            @Positive long roleId,
            @Positive long zoneId,
            @NotBlank
            @Pattern(regexp = "\\d{4}", message = "verificationCode must be the 4-digit in-game code")
            String verificationCode
    ) {}
}
