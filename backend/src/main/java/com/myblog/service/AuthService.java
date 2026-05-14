package com.myblog.service;

import com.myblog.dto.AdminUserDto;
import com.myblog.dto.LoginRequest;
import com.myblog.dto.LoginResponse;
import com.myblog.entity.User;
import com.myblog.repository.UserRepository;
import com.myblog.security.JwtProvider;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("邮箱或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("邮箱或密码错误");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getTokenVersion());

        return new LoginResponse(accessToken, refreshToken, AdminUserDto.from(user));
    }

    public LoginResponse refresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("refresh token 无效或已过期");
        }

        Claims claims = jwtProvider.parseToken(refreshToken);
        String userId = claims.getSubject();
        int tokenVersion = claims.get("tokenVersion", Integer.class);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("用户不存在"));

        if (user.getTokenVersion() != tokenVersion) {
            throw new BadCredentialsException("refresh token 已被撤销");
        }

        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getTokenVersion());

        return new LoginResponse(newAccessToken, newRefreshToken, AdminUserDto.from(user));
    }

    @Transactional
    public void logout() {
        User user = getCurrentUser();
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    public AdminUserDto getMe() {
        return AdminUserDto.from(getCurrentUser());
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadCredentialsException("未登录");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new BadCredentialsException("未登录");
    }
}
