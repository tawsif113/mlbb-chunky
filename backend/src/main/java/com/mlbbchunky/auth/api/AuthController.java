package com.mlbbchunky.auth.api;

import com.mlbbchunky.auth.application.AuthService;
import com.mlbbchunky.auth.application.MlbbIdentityProvider.VerifiedMlbbProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/mlbb")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/verification-code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendVerificationCode(@Valid @RequestBody VerificationCodeRequest request) {
        authService.sendVerificationCode(request.roleId(), request.zoneId());
    }

    @PostMapping("/verify")
    public VerifiedMlbbProfile verify(@Valid @RequestBody VerifyRequest request) {
        return authService.verify(request.roleId(), request.zoneId(), request.verificationCode());
    }

    public record VerificationCodeRequest(@Positive long roleId, @Positive long zoneId) {}

    public record VerifyRequest(
            @Positive long roleId,
            @Positive long zoneId,
            @NotBlank String verificationCode
    ) {}
}
