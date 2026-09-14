package com.kdongdexample.norunnolifeexample.service;

import com.kdongdexample.norunnolifeexample.domain.User;
import com.kdongdexample.norunnolifeexample.dto.NicknameAvailabilityResponse;
import com.kdongdexample.norunnolifeexample.exception.AuthenticatedUserNotFoundException;
import com.kdongdexample.norunnolifeexample.exception.InvalidNicknameException;
import com.kdongdexample.norunnolifeexample.exception.NicknameAlreadyExistsException;
import com.kdongdexample.norunnolifeexample.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class NicknameService {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣]{2,10}$");

    private final UserRepository userRepository;
    private final List<String> bannedWords;

    public NicknameService(UserRepository userRepository,
                           @Value("${nickname.banned-words}") String bannedWordsConfig) {
        this.userRepository = userRepository;
        this.bannedWords = Arrays.stream(bannedWordsConfig.split(","))
                .map(word -> word.trim().toLowerCase(Locale.ROOT))
                .filter(word -> !word.isBlank())
                .toList();
    }

    // GET /users/nicknames/availability 전용 - 예외 대신 결과값으로 응답합니다.
    // 이 엔드포인트는 인증이 없어도 호출 가능하고 항상 200이어야 하기 때문입니다.
    public NicknameAvailabilityResponse checkAvailability(String nickname) {
        try {
            validateFormat(nickname);
        } catch (InvalidNicknameException e) {
            return new NicknameAvailabilityResponse(false, e.getMessage());
        }

        if (isDuplicate(normalize(nickname))) {
            return new NicknameAvailabilityResponse(false, NicknameAlreadyExistsException.MESSAGE);
        }
        return new NicknameAvailabilityResponse(true, null);
    }

    // PUT /users/me/nickname 전용 - 실패 시 예외를 던져서 GlobalExceptionHandler가 400/409로 변환합니다.
    @Transactional
    public void changeNickname(Long userId, String nickname) {
        validateFormat(nickname);

        String normalized = normalize(nickname);
        if (isDuplicate(normalized)) {
            throw new NicknameAlreadyExistsException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));
        user.updateNickname(nickname);


        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new NicknameAlreadyExistsException();
        }
    }

    private void validateFormat(String nickname) {
        if (nickname == null || !NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new InvalidNicknameException("닉네임은 2~10자의 한글/영문/숫자만 가능합니다");
        }

        String normalized = normalize(nickname);
        boolean containsBannedWord = bannedWords.stream().anyMatch(normalized::contains);
        if (containsBannedWord) {
            throw new InvalidNicknameException("사용할 수 없는 닉네임입니다");
        }
    }

    private boolean isDuplicate(String normalized) {
        return userRepository.existsByNicknameNormalized(normalized);
    }

    private String normalize(String nickname) {
        return nickname.toLowerCase(Locale.ROOT);
    }
}
