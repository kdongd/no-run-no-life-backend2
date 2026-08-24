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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private GoogleIdTokenValidator googleIdTokenValidator;

    // loginWithGoogle 내부에서 TransactionTemplate으로 감싸는 DB 처리 구간(findByEmail/save)에 필요.
    // 스텁 없이 그대로 둬도 TransactionTemplate.execute()가 getTransaction()/commit()을 호출은 하지만
    // Mockito 기본 동작(unstubbed 메서드는 null 반환/no-op)만으로 콜백이 정상 실행된다.
    @Mock
    private PlatformTransactionManager transactionManager;

    private AuthService service() {
        return new AuthService(userRepository, passwordEncoder, jwtTokenProvider, googleIdTokenValidator, transactionManager);
    }

    private User createLocalUserWithId(Long id, String email, String encodedPassword) {
        User user = User.create(email, encodedPassword);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private User createGoogleUserWithId(Long id, String email, String googleUserId) {
        User user = User.createOAuth(email, AuthProvider.GOOGLE, googleUserId);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private GoogleIdToken.Payload verifiedPayload(String email, boolean emailVerified, String subject) {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(email);
        payload.setEmailVerified(emailVerified);
        payload.setSubject(subject);
        return payload;
    }

    // ===== signup =====

    @Test
    @DisplayName("회원가입 성공 시 비밀번호를 인코딩해서 저장한다")
    void signup_success() {
        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(passwordEncoder.encode("password1234")).willReturn("encoded-password");

        service().signup(new SignupRequest("new@test.com", "password1234"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("new@test.com");
        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    @DisplayName("이미 가입된 이메일로 회원가입하면 EmailAlreadyExistsException이 발생하고 저장은 호출되지 않는다")
    void signup_emailAlreadyExists_throwsException() {
        given(userRepository.existsByEmail("dup@test.com")).willReturn(true);

        assertThatThrownBy(() -> service().signup(new SignupRequest("dup@test.com", "password1234")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    // ===== login =====

    @Test
    @DisplayName("이메일과 비밀번호가 일치하면 토큰을 발급한다")
    void login_success() {
        User user = createLocalUserWithId(1L, "user@test.com", "encoded-password");
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password1234", "encoded-password")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(1L, "user@test.com")).willReturn("access-token");

        TokenResponse response = service().login(new LoginRequest("user@test.com", "password1234"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 InvalidCredentialsException이 발생한다")
    void login_userNotFound_throwsInvalidCredentialsException() {
        given(userRepository.findByEmail("none@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().login(new LoginRequest("none@test.com", "password1234")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 InvalidCredentialsException이 발생한다")
    void login_wrongPassword_throwsInvalidCredentialsException() {
        User user = createLocalUserWithId(1L, "user@test.com", "encoded-password");
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

        assertThatThrownBy(() -> service().login(new LoginRequest("user@test.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("비밀번호가 없는 OAuth 전용 계정으로 로그인 시도하면 matches()를 호출하지 않고 InvalidCredentialsException이 발생한다")
    void login_oauthOnlyAccount_throwsWithoutCallingPasswordEncoder() {
        User oauthUser = createGoogleUserWithId(1L, "oauth@test.com", "google-sub-1");
        given(userRepository.findByEmail("oauth@test.com")).willReturn(Optional.of(oauthUser));

        assertThatThrownBy(() -> service().login(new LoginRequest("oauth@test.com", "aaaaaaaa")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    // ===== loginWithGoogle =====

    @Test
    @DisplayName("가입 이력이 없는 이메일로 구글 로그인하면 신규 계정을 생성하고 토큰을 발급한다")
    void loginWithGoogle_newUser_createsAccountAndReturnsToken() {
        GoogleIdToken.Payload payload = verifiedPayload("new-google@test.com", true, "google-sub-2");
        given(googleIdTokenValidator.verify("valid-id-token")).willReturn(payload);
        given(userRepository.findByEmail("new-google@test.com")).willReturn(Optional.empty());
        User created = createGoogleUserWithId(2L, "new-google@test.com", "google-sub-2");
        given(userRepository.save(any(User.class))).willReturn(created);
        given(jwtTokenProvider.createAccessToken(2L, "new-google@test.com")).willReturn("google-access-token");

        TokenResponse response = service().loginWithGoogle(new GoogleLoginRequest("valid-id-token"));

        assertThat(response.accessToken()).isEqualTo("google-access-token");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(captor.getValue().getProviderId()).isEqualTo("google-sub-2");

        // DB 처리(findByEmail/save)가 TransactionTemplate으로 감싸진 트랜잭션 안에서 실행됐는지 확인
        verify(transactionManager).getTransaction(any());
        verify(transactionManager).commit(any());
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 신규 계정을 만들지 않고 기존 계정으로 로그인 처리한다 (자동 연결, provider는 갱신하지 않음)")
    void loginWithGoogle_existingUser_doesNotCreateNewAccountOrChangeProvider() {
        GoogleIdToken.Payload payload = verifiedPayload("existing@test.com", true, "google-sub-3");
        given(googleIdTokenValidator.verify("valid-id-token")).willReturn(payload);
        User existing = createLocalUserWithId(3L, "existing@test.com", "encoded-password");
        given(userRepository.findByEmail("existing@test.com")).willReturn(Optional.of(existing));
        given(jwtTokenProvider.createAccessToken(3L, "existing@test.com")).willReturn("existing-access-token");

        TokenResponse response = service().loginWithGoogle(new GoogleLoginRequest("valid-id-token"));

        assertThat(response.accessToken()).isEqualTo("existing-access-token");
        verify(userRepository, never()).save(any());
        // provider/providerId를 갱신하지 않으므로 LOCAL로 가입한 계정은 구글 로그인 후에도 계속 LOCAL로 남는다
        assertThat(existing.getProvider()).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    @DisplayName("이메일이 인증되지 않은 구글 토큰이면 InvalidGoogleTokenException이 발생하고 트랜잭션이 시작되지 않는다")
    void loginWithGoogle_emailNotVerified_throwsInvalidGoogleTokenException() {
        GoogleIdToken.Payload payload = verifiedPayload("unverified@test.com", false, "google-sub-4");
        given(googleIdTokenValidator.verify("valid-id-token")).willReturn(payload);

        assertThatThrownBy(() -> service().loginWithGoogle(new GoogleLoginRequest("valid-id-token")))
                .isInstanceOf(InvalidGoogleTokenException.class);

        verify(userRepository, never()).findByEmail(any());
        verify(transactionManager, never()).getTransaction(any());
    }

    @Test
    @DisplayName("구글 토큰 검증기가 예외를 던지면 그대로 전파되고 트랜잭션이 시작되지 않는다")
    void loginWithGoogle_invalidToken_propagatesExceptionWithoutStartingTransaction() {
        given(googleIdTokenValidator.verify("invalid-token")).willThrow(new InvalidGoogleTokenException());

        assertThatThrownBy(() -> service().loginWithGoogle(new GoogleLoginRequest("invalid-token")))
                .isInstanceOf(InvalidGoogleTokenException.class);

        verify(userRepository, never()).findByEmail(any());
        verify(transactionManager, never()).getTransaction(any());
    }
}
