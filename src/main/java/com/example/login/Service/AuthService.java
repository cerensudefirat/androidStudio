package com.example.login.Service;

import com.example.login.dto.AuthRequest;
import com.example.login.Entity.User;
import com.example.login.repository.UserRepository;
import com.example.login.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Kayıt Olma
    public String register(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Bu kullanıcı adı zaten var!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // Şifreyi Hash'le
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        return "Kayıt başarılı!";
    }

    // Giriş Yapma
    public String login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // Şifre Kontrolü
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Şifre yanlış!");
        }

        // Token Üret
        return jwtUtil.generateToken(user.getUsername());
    }
}