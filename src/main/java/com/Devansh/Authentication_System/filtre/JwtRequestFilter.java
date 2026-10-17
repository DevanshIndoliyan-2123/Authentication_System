package com.Devansh.Authentication_System.filtre;

import com.Devansh.Authentication_System.service.AppUserDetailsService;
import com.Devansh.Authentication_System.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwtToken = null;

        // Check whether Authorization header contains Bearer token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            jwtToken = authorizationHeader.substring(7);

            try {
                email = jwtUtil.extractEmail(jwtToken);
            } catch (Exception e) {
                // Invalid JWT
                filterChain.doFilter(request, response);
                return;
            }
        }

        if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            try {

                UserDetails userDetails =
                        appUserDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(jwtToken, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (Exception e) {
                // User doesn't exist / token invalid
                // Don't allow the JWT filter to produce a 500
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}