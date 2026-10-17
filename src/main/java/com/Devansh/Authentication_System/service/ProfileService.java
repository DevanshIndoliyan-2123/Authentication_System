package com.Devansh.Authentication_System.service;

import com.Devansh.Authentication_System.io.ProfileRequest;
import com.Devansh.Authentication_System.io.ProfileResponse;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);
    void sendResetOtp(String email);
    void resetPassword(String email, String otp, String newPassword);
    void sendOtp(String email);
    void verifyOtp(String email, String otp);

}
