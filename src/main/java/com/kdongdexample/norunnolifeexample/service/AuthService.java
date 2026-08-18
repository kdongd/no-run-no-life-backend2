package com.kdongdexample.norunnolifeexample.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.kdongdexample.norunnolifeexample.domain.AuthProvider;
import com.kdongdexample.norunnolifeexample.domain.User;
import com.kdongdexample.norunnolifeexample.dto.GoogleLoginRequest;
import com.kdongdexample.norunnolifeexample.dto.LoginRequest;
import com.kdongdexample.norunnolifeexample.dto.SignupRequest;
import com.kdongdexample.norunnolifeexample.dto.TokenResponse;
import com.kdongdexample.norunnolifeexample.exception.EmailAlreadyExistsException;
import com.kdongdexample.norunnolifeexample.exception.InvalidCredentialsException;
import com.kdongdexample.norunnolifeexample.exception.InvalidGoogleTokenException;
import com.kdongdexample.norunnolifeexample.repository.UserRepository;
import com.kdongdexample.norunnolifeexample.security.GoogleIdTokenValidator;
import com.kdongdexample.norunnolifeexample.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleIdTokenValidator googleIdTokenValidator;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       GoogleIdTokenValidator googleIdTokenValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleIdTokenValidator = googleIdTokenValidator;
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = User.create(request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        // OAuth 전용 계정(비밀번호 없음)이 일반 로그인을 시도하는 경우 방어
        if (!user.hasPassword() || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return TokenResponse.of(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail()));
    }

    @Transactional
    public TokenResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = googleIdTokenValidator.verify(request.idToken());

        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new InvalidGoogleTokenException();
        }

        String email = payload.getEmail();
        String googleUserId = payload.getSubject();

        // 이메일이 같으면 기존 계정(로컬 가입이든 이미 구글 연동이든)에 그대로 로그인 처리 - 자동 연결
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.createOAuth(email, AuthProvider.GOOGLE, googleUserId)));

        return TokenResponse.of(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail()));
    }
}
