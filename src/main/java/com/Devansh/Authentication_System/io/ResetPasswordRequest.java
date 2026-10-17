package com.Devansh.Authentication_System.io;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotNull(message="NewPassword cannot be null")
    private String newPassword;
    @NotNull(message="OTP cannot be null")
    private String otp;
    @NotNull(message="Email cannot be null")
    private String email;
}
