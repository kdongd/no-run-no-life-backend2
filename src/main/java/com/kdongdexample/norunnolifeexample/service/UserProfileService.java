package com.kdongdexample.norunnolifeexample.service;

import com.kdongdexample.norunnolifeexample.domain.User;
import com.kdongdexample.norunnolifeexample.domain.UserProfile;
import com.kdongdexample.norunnolifeexample.dto.UserProfileRequest;
import com.kdongdexample.norunnolifeexample.exception.AuthenticatedUserNotFoundException;
import com.kdongdexample.norunnolifeexample.exception.UserProfileAlreadyExistsException;
import com.kdongdexample.norunnolifeexample.repository.UserProfileRepository;
import com.kdongdexample.norunnolifeexample.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfile createProfile(Long userId, UserProfileRequest request) {
        if (userProfileRepository.existsByUserId(userId)) {
            throw new UserProfileAlreadyExistsException(userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        UserProfile profile = UserProfile.create(
                user,
                request.gender(),
                request.age(),
                request.heightCm(),
                request.weightKg(),
                request.experienceLevel(),
                request.primaryExercises(),
                request.goal()
        );

        return userProfileRepository.save(profile);
    }
}
