package com.Devansh.Authentication_System.controller;

import com.Devansh.Authentication_System.io.ProfileRequest;
import com.Devansh.Authentication_System.io.ProfileResponse;
import com.Devansh.Authentication_System.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> register(
            @Valid @RequestBody ProfileRequest request) {

        return ResponseEntity.ok(
                profileService.createProfile(request)
        );
    }
}