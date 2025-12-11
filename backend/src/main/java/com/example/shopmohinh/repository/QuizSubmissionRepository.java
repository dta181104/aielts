package com.example.shopmohinh.repository;

import com.example.shopmohinh.entity.course.QuizSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmissionEntity, Long> {
    List<QuizSubmissionEntity> findByQuizId(Long quizId);
    List<QuizSubmissionEntity> findByUserId(Long userId);
    Optional<QuizSubmissionEntity> findByQuizIdAndUserId(Long quizId, Long userId);
    List<QuizSubmissionEntity> findAllByQuizIdAndUserId(Long quizId, Long userId);
}
