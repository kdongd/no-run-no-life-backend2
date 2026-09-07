package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // refresh() 전용 락 조회. 같은 토큰으로 동시 요청이 들어와도 두번째 요청이
    // 첫번째 트랜잭션이 끝날 때까지 대기하게 만들어서 RTR 재사용 탐지가 무력화되지 않게 합니다.
    // logout()처럼 단순 폐기만 하는 곳까지 락을 걸 이유는 없는것 같아서 별도 메서드로 분리 했습니다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RefreshToken r where r.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    // 사용 가능한 세션을 오래된 순으로 조회 -> 기기 수 제한 시 가장 오래된 것부터 정리합니다.
    List<RefreshToken> findByUserIdAndRevokedFalseOrderByIssuedAtAsc(Long userId);

    // 재사용 탐지 시 같은 체인을 전체 폐기합니다.
    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.tokenFamily = :tokenFamily")
    void revokeAllByTokenFamily(@Param("tokenFamily") String tokenFamily);
}
