package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // 사용 가능한 세션을 오래된 순으로 조회 -> 기기 수 제한 시 가장 오래된 것부터 정리합니다.
    List<RefreshToken> findByUserIdAndRevokedFalseOrderByIssuedAtAsc(Long userId);

    // 재사용 탐지 시 같은 체인을 전체 폐기합니다.
    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.tokenFamily = :tokenFamily")
    void revokeAllByTokenFamily(@Param("tokenFamily") String tokenFamily);
}
