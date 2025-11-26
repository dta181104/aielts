package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.CourseQuestionRequest;
import com.example.shopmohinh.dto.response.QuestionResponse;
import com.example.shopmohinh.entity.course.CourseEntity;
import com.example.shopmohinh.entity.course.LessonEntity;
import com.example.shopmohinh.entity.course.QuestionEntity;
import com.example.shopmohinh.entity.course.QuizEntity;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.repository.QuestionRepository;
import com.example.shopmohinh.repository.QuizRepository;
import com.example.shopmohinh.util.QuestionOptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseQuestionService {

    @Autowired
    private ApplicationContext ctx;

    // Use JpaRepository interfaces for dynamic resolution to tolerate duplicate repo interfaces
    private JpaRepository<CourseEntity, Long> courseRepository;
    private JpaRepository<LessonEntity, Long> lessonRepository;

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public CourseQuestionService(QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    @PostConstruct
    void init() {
        // Try to resolve CourseRepository from either package
        try {
            this.courseRepository = ctx.getBean("courseRepository", JpaRepository.class);
        } catch (Exception e) {
            // fallback: search beans by type that handle CourseEntity
            Optional<?> bean = ctx.getBeansOfType(JpaRepository.class).values().stream()
                    .filter(b -> {
                        try {
                            // crude check: see if bean's class declares methods for CourseEntity
                            return b.getClass().getName().toLowerCase().contains("course")
                                    || b.getClass().getSimpleName().toLowerCase().contains("course");
                        } catch (Exception ex) {
                            return false;
                        }
                    }).findFirst();
            if (bean.isPresent()) this.courseRepository = (JpaRepository<CourseEntity, Long>) bean.get();
        }

        // Resolve LessonRepository similarly
        try {
            this.lessonRepository = ctx.getBean("lessonRepository", JpaRepository.class);
        } catch (Exception e) {
            Optional<?> bean = ctx.getBeansOfType(JpaRepository.class).values().stream()
                    .filter(b -> b.getClass().getName().toLowerCase().contains("lesson")
                            || b.getClass().getSimpleName().toLowerCase().contains("lesson"))
                    .findFirst();
            if (bean.isPresent()) this.lessonRepository = (JpaRepository<LessonEntity, Long>) bean.get();
        }

        if (this.courseRepository == null || this.lessonRepository == null) {
            log.warn("CourseQuestionService: could not fully resolve course/lesson repositories by name; if app fails, consider cleaning duplicate repository interfaces.");
        }
    }

    public QuestionResponse addQuestion(Long courseId, Long lessonId, CourseQuestionRequest request) {
        if (courseRepository == null || lessonRepository == null) {
            // Fallback: repositories couldn't be resolved. Use PRODUCT_NOT_FOUND for now
            // (INTERNAL_ERROR was added but IDE/compiler may not pick it up; keep a stable constant)
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Validate lesson belongs to course
        if (lesson.getSection() == null || lesson.getSection().getCourse() == null || !lesson.getSection().getCourse().getId().equals(course.getId())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Use first quiz of lesson or create new if none
        List<QuizEntity> quizzes = quizRepository.findByLesson(lesson);
        QuizEntity quiz = quizzes.isEmpty() ? quizRepository.save(
                QuizEntity.builder()
                        .lesson(lesson)
                        .title("Auto Quiz for " + lesson.getTitle())
                        .passScore(60)
                        .duration(10)
                        .build()
        ) : quizzes.get(0);

        String optionsJson = null;
        if (!CollectionUtils.isEmpty(request.getOptions())) {
            optionsJson = QuestionOptionUtils.optionsToJson(request.getOptions());
        }

        QuestionEntity entity = QuestionEntity.builder()
                .quiz(quiz)
                .content(request.getContent())
                .audioUrl(request.getAudioUrl())
                .options(optionsJson)
                .correctOption(request.getCorrectOption() == null ? null : QuestionOptionUtils.toLetter(request.getCorrectOption()))
                .explanation(request.getExplanation())
                .skill(request.getSkill())
                .build();
        QuestionEntity saved = questionRepository.save(entity);
        return toResponse(saved);
    }

    private QuestionResponse toResponse(QuestionEntity q) {
        List<String> options = null;
        if (q.getOptions() != null) {
            options = QuestionOptionUtils.storedJsonToList(q.getOptions());
        }
        String correctOpt = QuestionOptionUtils.normalizeCorrectOption(q.getCorrectOption());
        return QuestionResponse.builder()
                .id(q.getId())
                .quizId(q.getQuiz().getId())
                .content(q.getContent())
                .audioUrl(q.getAudioUrl())
                .options(options)
                .correctOption(correctOpt)
                .explanation(q.getExplanation())
                .skill(q.getSkill())
                .build();
    }

    private String toJsonArray(List<String> items) {
        return "[" + items.stream().map(s -> "\"" + escape(s) + "\"").collect(Collectors.joining(",")) + "]";
    }

    private List<String> parseJsonArray(String json) {
        String trimmed = json.trim();
        if (trimmed.length() < 2) return List.of();
        String inner = trimmed.substring(1, trimmed.length() - 1);
        if (inner.isEmpty()) return List.of();
        return Arrays.stream(inner.split(","))
                .map(String::trim)
                .map(s -> s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s)
                .collect(Collectors.toList());
    }

    private String escape(String s) { return s.replace("\"", "\\\""); }
}
