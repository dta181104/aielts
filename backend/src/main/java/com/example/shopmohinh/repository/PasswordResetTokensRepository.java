package com.example.shopmohinh.repository;

import com.example.shopmohinh.entity.PasswordResetTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokensRepository extends JpaRepository<PasswordResetTokens, Long> {
    @Query(value = """
    select * from password_reset_tokens
    where token = :token
    """, nativeQuery = true)
    PasswordResetTokens findByToken(String token);
}
