package com.Devansh.Authentication_System.service;

import com.Devansh.Authentication_System.entity.UserEntity;
import com.Devansh.Authentication_System.io.ProfileRequest;
import com.Devansh.Authentication_System.io.ProfileResponse;
import com.Devansh.Authentication_System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor

public class ProfileServiceImpl  implements ProfileService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailServices emailServices;
    private String verifyOtp;
    private Long verifyOtpExpireAt;   // add this — was missing
    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        UserEntity saved = userRepository.save(convertToUserEntity(request));
        return convertToProfileResponse(saved);
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Generate a random OTP
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000); // 6-digit OTP

        // Set the OTP and its expiration time (e.g., 5 minutes from now)
        existingUser.setResetOtp(otp);
        existingUser.setResetOtpExpireAt(System.currentTimeMillis() + 5 * 60 * 1000); // 5 minutes in milliseconds

        userRepository.save(existingUser);

        // Here you would typically send the OTP to the user's email or phone number
        // For example, using an email service to send the OTP

        try{
            emailServices.sendResetPasswordMail(existingUser.getEmail(), existingUser.getResetOtp());

        }catch(Exception ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {

        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        // Check if OTP is valid and not expired
        if (existingUser.getResetOtp() == null
                || !existingUser.getResetOtp().equals(otp)
                || existingUser.getResetOtpExpireAt() < System.currentTimeMillis()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired OTP"
            );
        }

        // Check password length
        if (newPassword.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must be at least 8 characters long"
            );
        }

        // Check if new password is same as old password
        if (passwordEncoder.matches(newPassword, existingUser.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password cannot be the same as the old password"
            );
        }

        // Update password
        existingUser.setPassword(passwordEncoder.encode(newPassword));

        // Clear OTP after successful password reset
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpireAt(0L);

        userRepository.save(existingUser);
    }

    @Override
    public void sendOtp(String email) {

        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + email));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        existingUser.setVerifyOtp(otp);                                    // was setResetOtp
        existingUser.setVerifyOtpExpireAt(System.currentTimeMillis() + 5 * 60 * 1000); // was setResetOtpExpireAt

        userRepository.save(existingUser);

        try {
            emailServices.sendOtpEmail(existingUser.getEmail(), otp);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP", ex);
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + email));

        if (existingUser.getVerifyOtp() == null
                || !existingUser.getVerifyOtp().equals(otp)
                || existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()) {   // was getResetOtpExpireAt

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);          // was setResetOtp(null)
        existingUser.setVerifyOtpExpireAt(0L);    // was setResetOtpExpireAt(0L)

        userRepository.save(existingUser);
    }



    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .userId(newProfile.getUserId())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }
    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }

}
