package com.Devansh.Authentication_System.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServices {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")

    private String fromEmail;

    public void sendWelcomeMail(String toEmail, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to our Application");

        message.setText("Dear " + name + ",\n\nWelcome to our application! We are excited to have you on board.\n\nBest regards,\nThe Team");

        mailSender.send(message);
    }
    public void sendResetPasswordMail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");

        message.setText("Dear User,\n\nWe received a request to reset your password. Please use the following OTP to reset your password:\n\n" + otp + "\n\nIf you did not request a password reset, please ignore this email.\n\nBest regards,\nThe Team");

        mailSender.send(message);
    }
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");

        message.setText("Dear User,\n\nYour OTP code is: " + otp + "\n\nPlease use this code to complete your verification process.\n\nBest regards,\nThe Team");

        mailSender.send(message);
    }

}
