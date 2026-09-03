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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    // 계정이 없거나(OAuth 전용 계정 포함) 비밀번호 자체가 없는 경우에도 matches()를
    // 동일하게 한 번 호출시켜서 걸리는 시간을 맞추기 위한 더미 해시.
    // 실제 어떤 계정의 비밀번호도 아니고, BCrypt 연산 시간을 채우는 용도로만 쓴다.
    private static final String DUMMY_PASSWORD_HASH =
            "$2b$10$VNV8QWPKZdi5BrXTpnILDeHthOkTomdS2EezISxvZ1M37S3k.l..q";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleIdTokenValidator googleIdTokenValidator;
    private final TransactionTemplate transactionTemplate;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       GoogleIdTokenValidator googleIdTokenValidator,
                       PlatformTransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleIdTokenValidator = googleIdTokenValidator;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = User.create(request.email(), passwordEncoder.encode(request.password()));

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(request.email());
        }
    }

    public TokenResponse login(LoginRequest request) {
        Optional<User> maybeUser = userRepository.findByEmail(request.email());

        // 계정이 없거나(Optional.empty) OAuth 전용 계정(비밀번호 없음)이어도 더미 해시로
        // matches()를 동일하게 호출해서, "계정이 있고 비밀번호만 틀린 경우"와 걸리는 시간을
        // 맞춘다. 그렇지 않으면 메시지는 통일해놔도 응답 시간만으로 "이 이메일이 LOCAL
        // 계정으로 존재하는지"가 새어나간다(타이밍 사이드채널).
        String hashToCheck = maybeUser
                .filter(User::hasPassword)
                .map(User::getPassword)
                .orElse(DUMMY_PASSWORD_HASH);

        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToCheck);

        if (maybeUser.isEmpty() || !maybeUser.get().hasPassword() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        User user = maybeUser.get();
        return TokenResponse.of(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail()));
    }

    public TokenResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = googleIdTokenValidator.verify(request.idToken());

        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new InvalidGoogleTokenException();
        }

        String email = payload.getEmail();
        String googleUserId = payload.getSubject();

        User user = transactionTemplate.execute(status ->
                userRepository.findByEmail(email)
                        .orElseGet(() -> userRepository.save(User.createOAuth(email, AuthProvider.GOOGLE, googleUserId)))
        );

        return TokenResponse.of(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail()));
    }
}
