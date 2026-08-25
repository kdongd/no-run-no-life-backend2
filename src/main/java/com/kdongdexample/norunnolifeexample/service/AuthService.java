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

@Service
@Transactional(readOnly = true)
public class AuthService {

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

        // existsByEmail() 체크와 save() 사이에 동시에 같은 이메일로 가입 요청이 들어오면
        // 여기서도 통과해버릴 수 있음(원자적이지 않음). saveAndFlush()로 즉시 flush시켜
        // DB의 email unique 제약 위반을 이 자리에서 동기적으로 받아 409로 통일한다.
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(request.email());
        }
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

    // @Transactional 제거 — 구글 네트워크 호출이 트랜잭션(=DB 커넥션 점유) 밖에서 실행되게 함
    public TokenResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = googleIdTokenValidator.verify(request.idToken());

        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new InvalidGoogleTokenException();
        }

        String email = payload.getEmail();
        String googleUserId = payload.getSubject();

        // 이메일이 같으면 기존 계정으로 로그인만 처리한다. provider/providerId는 갱신하지 않으므로
        // LOCAL로 가입한 계정에 구글로 로그인해도 그 계정의 provider는 계속 LOCAL로 남는다.
        // (실제 계정 연동은 아직 미구현 — 카카오/네이버 등 멀티프로바이더 설계 시 함께 처리 예정)
        User user = transactionTemplate.execute(status ->
                userRepository.findByEmail(email)
                        .orElseGet(() -> userRepository.save(User.createOAuth(email, AuthProvider.GOOGLE, googleUserId)))
        );

        return TokenResponse.of(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail()));
    }
}
