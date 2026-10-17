package com.Devansh.Authentication_System.controller;

import com.Devansh.Authentication_System.io.AuthRequest;
import com.Devansh.Authentication_System.io.AuthResponse;
import com.Devansh.Authentication_System.io.ResetPasswordRequest;
import com.Devansh.Authentication_System.service.AppUserDetailsService;
import com.Devansh.Authentication_System.service.ProfileService;
import com.Devansh.Authentication_System.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
//working ok
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        try {

            authenticate(request.getEmail(), request.getPassword());
            final UserDetails userDetails = appUserDetailsService.loadUserByUsername(request.getEmail());
            final String jwtToken = jwtUtil.generateToken(userDetails);
            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict")
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie.toString())
                    .body(new AuthResponse(request.getEmail(), jwtToken));

        } catch (DisabledException ex) {

            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Authentication is disabled");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);

        } catch (Exception ex) {

            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Invalid email or password");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }
    }

    private Authentication authenticate(String email, String password) {

        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );
    }
//    working ok
    @GetMapping("/isAuthenticated")
    public ResponseEntity<Boolean> isAuthenticated(
            @CurrentSecurityContext(expression = "authentication")
            Authentication authentication) {

        boolean isAuthenticated =
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !(authentication instanceof AnonymousAuthenticationToken);

        return ResponseEntity.ok(isAuthenticated);
    }
    //working ok
    @PostMapping("/send-reset-otp")
    public ResponseEntity<?> sendResetOtp(@RequestParam("email") String email) {

        try {
            profileService.sendResetOtp(email);

            Map<String, Object> response = new HashMap<>();
            response.put("error", false);
            response.put("message", "OTP sent successfully to " + email);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {

            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }
    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try{
         profileService .resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }

    //working ok
    @PostMapping("/send-otp")
    public void sendVerifyOtp(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        try{
            profileService.sendOtp(email);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }


    }
//working ok
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyEmail(
            @RequestBody Map<String, Object> request,
            @CurrentSecurityContext(expression = "authentication?.name") String email) {

        Object otpValue = request.get("otp");
        if (otpValue == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is required");
        }

        profileService.verifyOtp(email, otpValue.toString());

        Map<String, Object> response = new HashMap<>();
        response.put("error", false);
        response.put("message", "Email verified successfully");
        return ResponseEntity.ok(response);
    }
}

