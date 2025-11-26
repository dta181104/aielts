package com.example.shopmohinh.repository;

import com.example.shopmohinh.entity.course.QuestionEntity;
import com.example.shopmohinh.entity.course.QuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    List<QuestionEntity> findByQuiz(QuizEntity quiz);
}

