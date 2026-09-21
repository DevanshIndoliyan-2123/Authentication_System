package com.Devansh.Authentication_System.io;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NotNull

public class AuthRequest {
    private String email;
    private String password;
}
