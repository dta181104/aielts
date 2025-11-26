package com.example.shopmohinh.repository;

import com.example.shopmohinh.entity.course.QuizEntity;
import com.example.shopmohinh.entity.course.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<QuizEntity, Long> {
    List<QuizEntity> findByLesson(LessonEntity lesson);
}

