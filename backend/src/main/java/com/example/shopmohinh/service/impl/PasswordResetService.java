package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }


    public String forgotPassword(String email, String username) {
        User account = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if(!account.getUsername().equals(username)){
            throw new AppException(ErrorCode.USERNAME_OR_EMAIL_INVALID);
        }

        String token = UUID.randomUUID().toString();
        String redisKey = "password_reset:" + email;

        // Lưu token vào Redis với TTL = 30 phút
        redisTemplate.opsForValue().set(redisKey, token, Duration.ofMinutes(30));

        this.sendEmail(email, "Shopmohinh cấp mã quên mật khẩu",
                "Mã: " + token);

        return "Password reset email sent.";
    }

//    public static String generateRandomPassword(int length) {
//        // Định nghĩa các ký tự có thể có trong mật khẩu
//        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
//        String lowerCaseChars = "abcdefghijklmnopqrstuvwxyz";
//        String numbers = "0123456789";
//        String specialChars = "!@#$%^&*()-_=+[]{}|;:,.<>?";
//
//        // Kết hợp tất cả các ký tự
//        String allChars = upperCaseChars + lowerCaseChars + numbers + specialChars;
//
//        // Sử dụng SecureRandom để tạo số ngẫu nhiên
//        SecureRandom random = new SecureRandom();
//
//        StringBuilder password = new StringBuilder(length);
//
//        // Sinh ra mật khẩu ngẫu nhiên
//        for (int i = 0; i < length; i++) {
//            int index = random.nextInt(allChars.length());  // Chọn ngẫu nhiên một ký tự từ tất cả các ký tự có sẵn
//            password.append(allChars.charAt(index));
//        }
//
//        return password.toString();
//    }

    //Kiểm tra token
    public String verifyVerificationCode(String email, String token) {
        String redisKey = "password_reset:" + email;
        String savedToken = redisTemplate.opsForValue().get(redisKey);

        if (savedToken == null) {
            throw new AppException(ErrorCode.VERIFICATION_TOKEN_NOT_FOUND);
        }

        if (!savedToken.equals(token)) {
            throw new AppException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        return "Verification code is valid.";
    }

    //Kiểm tra token
    public String resetPassword(String email,String newPassword) {
        Optional<User> account = userRepository.findByEmail(email);
        if (account.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        User newAccount = account.get();
        newAccount.setPass(passwordEncoder.encode(newPassword));
        userRepository.save(newAccount);

        return "password has been reset.";
    }
}
