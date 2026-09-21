package com.Devansh.Authentication_System.controller;

import com.Devansh.Authentication_System.io.AuthRequest;
import com.Devansh.Authentication_System.io.AuthResponse;
import com.Devansh.Authentication_System.service.AppUserDetailsService;
import com.Devansh.Authentication_System.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

