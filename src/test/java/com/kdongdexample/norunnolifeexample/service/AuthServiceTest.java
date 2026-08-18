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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private AuthService service() {
        return new AuthService(userRepository, passwordEncoder, jwtTokenProvider, googleIdTokenValidator);
    }

    private static User createLocalUserWithId(Long id) {
        User user = User.create("test@test.com", "encoded-password");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static User createGoogleUserWithId(Long id, String email) {
        User user = User.createOAuth(email, AuthProvider.GOOGLE, "google-sub");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static GoogleIdToken.Payload verifiedPayload(String email, boolean emailVerified) {
        return new GoogleIdToken.Payload()
                .setEmail(email)
                .setEmailVerified(emailVerified)
                .setSubject("google-sub");
    }

    @Test
    @DisplayName("signup - 신규 이메일이면 저장한다")
    void signup_savesUser_whenEmailIsNew() {
        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(passwordEncoder.encode("password1234")).willReturn("encoded");

        service().signup(new SignupRequest("new@test.com", "password1234"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("signup - 이미 존재하는 이메일이면 예외")
    void signup_throwsException_whenEmailAlreadyExists() {
        given(userRepository.existsByEmail("dup@test.com")).willReturn(true);

        assertThatThrownBy(() -> service().signup(new SignupRequest("dup@test.com", "password1234")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login - 올바른 비밀번호면 토큰을 반환한다")
    void login_returnsToken_whenPasswordIsCorrect() {
        User user = createLocalUserWithId(1L);
        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password1234", "encoded-password")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(1L, "test@test.com")).willReturn("jwt-token");

        TokenResponse response = service().login(new LoginRequest("test@test.com", "password1234"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("login - 비밀번호가 틀리면 예외")
    void login_throwsException_whenPasswordIsWrong() {
        User user = createLocalUserWithId(1L);
        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> service().login(new LoginRequest("test@test.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login - 존재하지 않는 이메일이면 예외")
    void login_throwsException_whenEmailNotFound() {
        given(userRepository.findByEmail("none@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().login(new LoginRequest("none@test.com", "password1234")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login - 구글 전용 계정이 비밀번호 로그인을 시도하면 예외")
    void login_throwsException_whenGoogleOnlyAccountAttemptsPasswordLogin() {
        User googleUser = createGoogleUserWithId(1L, "oauth@test.com");
        given(userRepository.findByEmail("oauth@test.com")).willReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> service().login(new LoginRequest("oauth@test.com", "aaaaaaaa")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("loginWithGoogle - 신규 이메일이면 계정을 새로 만들고 토큰을 반환한다")
    void loginWithGoogle_createsNewUserAndReturnsToken_whenEmailIsNew() {
        given(googleIdTokenValidator.verify("valid-id-token"))
                .willReturn(verifiedPayload("new-google@test.com", true));
        given(userRepository.findByEmail("new-google@test.com")).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(createGoogleUserWithId(2L, "new-google@test.com"));
        given(jwtTokenProvider.createAccessToken(2L, "new-google@test.com")).willReturn("jwt-token");

        TokenResponse response = service().loginWithGoogle(new GoogleLoginRequest("valid-id-token"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("loginWithGoogle - 이미 같은 이메일의 로컬 계정이 있으면 자동 연결되어 그 계정으로 로그인된다")
    void loginWithGoogle_autoLinksExistingLocalAccount_whenEmailMatches() {
        User existingLocalUser = createLocalUserWithId(1L);
        given(googleIdTokenValidator.verify("valid-id-token"))
                .willReturn(verifiedPayload("test@test.com", true));
        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(existingLocalUser));
        given(jwtTokenProvider.createAccessToken(1L, "test@test.com")).willReturn("jwt-token");

        TokenResponse response = service().loginWithGoogle(new GoogleLoginRequest("valid-id-token"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("loginWithGoogle - 이메일 미인증 토큰이면 예외")
    void loginWithGoogle_throwsException_whenEmailNotVerified() {
        given(googleIdTokenValidator.verify("unverified-token"))
                .willReturn(verifiedPayload("test@test.com", false));

        assertThatThrownBy(() -> service().loginWithGoogle(new GoogleLoginRequest("unverified-token")))
                .isInstanceOf(InvalidGoogleTokenException.class);

        verify(userRepository, never()).findByEmail(any());
    }
}
