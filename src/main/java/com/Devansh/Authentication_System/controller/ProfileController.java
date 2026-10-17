
        package com.Devansh.Authentication_System.controller;

import com.Devansh.Authentication_System.io.ProfileRequest;
import com.Devansh.Authentication_System.io.ProfileResponse;
import com.Devansh.Authentication_System.service.EmailServices;
import com.Devansh.Authentication_System.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProfileController {

    private final ProfileService profileService;
    private final EmailServices emailServices;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody ProfileRequest request) {

        // Create user first
        ProfileResponse response = profileService.createProfile(request);

        try {

            // Send welcome email
            emailServices.sendWelcomeMail(
                    response.getEmail(),
                    response.getName()
            );

            return ResponseEntity.ok(response);

        } catch (MailException ex) {

            Map<String, Object> error = new HashMap<>();

            error.put("error", true);
            error.put("message",
                    "Registration successful, but email service is not working. "
                            + "Your account was created, but the welcome email could not be sent.");

            error.put("emailSent", false);

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(error);
        }
    }

    // For testing purpose. Remove this endpoint in production.
    @GetMapping("/test")
    public String test() {
        return "Auth is Working";
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(
            @CurrentSecurityContext(
                    expression = "authentication.name"
            )
            String email) {

        return profileService.getProfile(email);
    }
}