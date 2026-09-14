package com.kdongdexample.norunnolifeexample.service;

import com.kdongdexample.norunnolifeexample.domain.User;
import com.kdongdexample.norunnolifeexample.dto.NicknameAvailabilityResponse;
import com.kdongdexample.norunnolifeexample.exception.AuthenticatedUserNotFoundException;
import com.kdongdexample.norunnolifeexample.exception.InvalidNicknameException;
import com.kdongdexample.norunnolifeexample.exception.NicknameAlreadyExistsException;
import com.kdongdexample.norunnolifeexample.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NicknameServiceTest {

    private static final String BANNED_WORDS = "admin,운영자,norunnolife";

    @Mock
    private UserRepository userRepository;

    private NicknameService service() {
        return new NicknameService(userRepository, BANNED_WORDS);
    }

    private User userWithId(Long id) {
        User user = User.create("test@test.com", "encoded-password");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    // ===== checkAvailability =====

    @Test
    @DisplayName("형식이 올바르고 중복도 없으면 available=true를 반환한다")
    void checkAvailability_available() {
        given(userRepository.existsByNicknameNormalized("runner")).willReturn(false);

        NicknameAvailabilityResponse response = service().checkAvailability("runner");

        assertThat(response.available()).isTrue();
        assertThat(response.reason()).isNull();
    }

    @Test
    @DisplayName("형식이 올바르지 않으면 예외를 던지지 않고 available=false와 사유를 반환한다")
    void checkAvailability_invalidFormat_returnsUnavailableWithoutThrowing() {
        NicknameAvailabilityResponse response = service().checkAvailability("a");

        assertThat(response.available()).isFalse();
        assertThat(response.reason()).isNotBlank();
        verify(userRepository, never()).existsByNicknameNormalized(any());
    }

    @Test
    @DisplayName("금지어가 포함되어 있으면 available=false를 반환한다")
    void checkAvailability_bannedWord_returnsUnavailable() {
        NicknameAvailabilityResponse response = service().checkAvailability("admin123");

        assertThat(response.available()).isFalse();
    }

    @Test
    @DisplayName("대소문자만 다른 닉네임이 이미 있으면 available=false를 반환한다")
    void checkAvailability_caseInsensitiveDuplicate_returnsUnavailable() {
        given(userRepository.existsByNicknameNormalized("runner")).willReturn(true);

        NicknameAvailabilityResponse response = service().checkAvailability("RunNer");

        assertThat(response.available()).isFalse();
        assertThat(response.reason()).isEqualTo(NicknameAlreadyExistsException.MESSAGE);
    }

    // ===== changeNickname =====

    @Test
    @DisplayName("형식이 올바르고 중복이 없으면 닉네임을 변경하고 저장한다")
    void changeNickname_success() {
        User user = userWithId(1L);
        given(userRepository.existsByNicknameNormalized("runner")).willReturn(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        service().changeNickname(1L, "runner");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNickname()).isEqualTo("runner");
    }

    @Test
    @DisplayName("형식이 올바르지 않으면 InvalidNicknameException이 발생하고 저장은 호출되지 않는다")
    void changeNickname_invalidFormat_throwsException() {
        assertThatThrownBy(() -> service().changeNickname(1L, "a"))
                .isInstanceOf(InvalidNicknameException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("대소문자만 다른 닉네임이 이미 존재하면 NicknameAlreadyExistsException(409)이 발생한다")
    void changeNickname_caseInsensitiveDuplicate_throwsException() {
        given(userRepository.existsByNicknameNormalized("runner")).willReturn(true);

        assertThatThrownBy(() -> service().changeNickname(1L, "RunNer"))
                .isInstanceOf(NicknameAlreadyExistsException.class);

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("existsBy 체크는 통과했지만 동시 요청으로 DB unique 제약에 걸리면 NicknameAlreadyExistsException(409)로 변환된다")
    void changeNickname_concurrentDuplicateOnSave_throwsException() {
        User user = userWithId(1L);
        given(userRepository.existsByNicknameNormalized("runner")).willReturn(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.saveAndFlush(any())).willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service().changeNickname(1L, "runner"))
                .isInstanceOf(NicknameAlreadyExistsException.class);
    }

    @Test
    @DisplayName("인증된 사용자를 찾을 수 없으면 AuthenticatedUserNotFoundException이 발생한다")
    void changeNickname_userNotFound_throwsException() {
        given(userRepository.existsByNicknameNormalized("runner")).willReturn(false);
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().changeNickname(1L, "runner"))
                .isInstanceOf(AuthenticatedUserNotFoundException.class);
    }
}
