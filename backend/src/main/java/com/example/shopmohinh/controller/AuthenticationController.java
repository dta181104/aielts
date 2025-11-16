package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.*;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.AuthenticationResponse;
import com.example.shopmohinh.dto.response.IntrospectResponse;
import com.example.shopmohinh.service.impl.AuthenticationService;
import com.example.shopmohinh.service.impl.PasswordResetService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
//Thay thế cho @Autowired
//@RequiredArgsConstructor sẽ tự động tạo contructor của những method đc khai báo là final
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    PasswordResetService passwordResetService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();

    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();

    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();

    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefeshRequest request) throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();

    }

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestParam String email, @RequestParam String username ) {
        return ApiResponse.<String>builder()
                .result(passwordResetService.forgotPassword(email, username))
                .build();
    }

    @PostMapping("/verify-code")
    public ApiResponse<String> verifyVerificationCode(@RequestParam String email, @RequestParam String token) {
        return ApiResponse.<String>builder()
                .result(passwordResetService.verifyVerificationCode(email, token))
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        return ApiResponse.<String>builder()
                .result(passwordResetService.resetPassword(email, newPassword))
                .build();
    }
}
