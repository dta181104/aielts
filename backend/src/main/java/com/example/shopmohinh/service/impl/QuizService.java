package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.QuestionRequest;
import com.example.shopmohinh.dto.request.QuizRequest;
import com.example.shopmohinh.dto.response.QuestionResponse;
import com.example.shopmohinh.dto.response.QuizResponse;
import com.example.shopmohinh.entity.course.*;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.repository.course.LessonRepository;
import com.example.shopmohinh.repository.QuizRepository;
import com.example.shopmohinh.repository.QuestionRepository;
import com.example.shopmohinh.util.QuestionOptionUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizService {

    QuizRepository quizRepository;
    QuestionRepository questionRepository;
    LessonRepository lessonRepository;

    public QuizResponse createQuiz(QuizRequest request) {
        LessonEntity lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        QuizEntity quiz = QuizEntity.builder()
                .lesson(lesson)
                .title(request.getTitle())
                .passScore(request.getPassScore())
                .duration(request.getDuration())
                .build();

        QuizEntity saved = quizRepository.save(quiz);
        return toQuizResponse(saved, new ArrayList<>());
    }

    public QuestionResponse addQuestion(QuestionRequest request) {
        QuizEntity quiz;
        if (request.getQuizId() != null) {
            quiz = quizRepository.findById(request.getQuizId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        } else if (request.getLessonId() != null) {
            LessonEntity lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            quiz = QuizEntity.builder()
                    .lesson(lesson)
                    .title("Auto Quiz for " + lesson.getTitle())
                    .passScore(60)
                    .duration(10)
                    .build();
            quiz = quizRepository.save(quiz);
        } else {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        String optionsJson = null;
        if (!CollectionUtils.isEmpty(request.getOptions())) {
            // Simple JSON array creation
            optionsJson = toJsonArray(request.getOptions());
        }

        QuestionEntity q = QuestionEntity.builder()
                .quiz(quiz)
                .content(request.getContent())
                .audioUrl(request.getAudioUrl())
                .options(optionsJson)
                .correctOption(request.getCorrectOption() == null ? null : String.valueOf(request.getCorrectOption()))
                .explanation(request.getExplanation())
                .skill(request.getSkill())
                .build();

        QuestionEntity saved = questionRepository.save(q);
        return toQuestionResponse(saved);
    }

    public QuizResponse getQuiz(Long id) {
        QuizEntity quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        List<QuestionEntity> questions = questionRepository.findByQuiz(quiz);
        List<QuestionResponse> questionResponses = questions.stream()
                .map(this::toQuestionResponse)
                .collect(Collectors.toList());
        return toQuizResponse(quiz, questionResponses);
    }

    private QuizResponse toQuizResponse(QuizEntity quiz, List<QuestionResponse> questions) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .lessonId(quiz.getLesson().getId())
                .title(quiz.getTitle())
                .passScore(quiz.getPassScore())
                .duration(quiz.getDuration())
                .questions(questions)
                .build();
    }

    private QuestionResponse toQuestionResponse(QuestionEntity q) {
        List<String> options = null;
        if (q.getOptions() != null) {
            options = parseJsonArray(q.getOptions());
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
        return "[" + items.stream()
                .map(s -> "\"" + escape(s) + "\"")
                .collect(Collectors.joining(",")) + "]";
    }

    private List<String> parseJsonArray(String json) {
        // naive parsing: remove brackets and split by comma not handling escaped commas
        String trimmed = json.trim();
        if (trimmed.length() < 2) return new ArrayList<>();
        String inner = trimmed.substring(1, trimmed.length() - 1);
        if (inner.isEmpty()) return new ArrayList<>();
        return Arrays.stream(inner.split(","))
                .map(s -> s.trim())
                .map(s -> s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s)
                .collect(Collectors.toList());
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}
