package com.example.shopmohinh.controller.course;

import com.example.shopmohinh.dto.request.QuestionRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.QuestionResponse;
import com.example.shopmohinh.entity.course.QuestionEntity;
import com.example.shopmohinh.entity.course.QuizEntity;
import com.example.shopmohinh.repository.QuestionRepository;
import com.example.shopmohinh.repository.QuizRepository;
import com.example.shopmohinh.util.JsonUtils;
import com.example.shopmohinh.util.QuestionOptionUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionAdminController {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    @PostMapping("/quiz/{quizId}")
    public ApiResponse<QuestionResponse> create(@PathVariable Long quizId, @RequestBody @Valid QuestionRequest request) {
        QuizEntity quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) {
            return ApiResponse.<QuestionResponse>builder().message("Quiz not found").build();
        }
        QuestionEntity q = QuestionEntity.builder()
                .quiz(quiz)
                .content(request.getContent())
                .audioUrl(request.getAudioUrl())
                .options(request.getOptions() != null ? QuestionOptionUtils.optionsToJson(request.getOptions()) : null)
                .correctOption(request.getCorrectOption() != null ? QuestionOptionUtils.toLetter(request.getCorrectOption()) : null)
                .explanation(request.getExplanation())
                .skill(request.getSkill())
                .build();
        QuestionEntity saved = questionRepository.save(q);
        return ApiResponse.<QuestionResponse>builder().result(toResponse(saved)).build();
    }

    @GetMapping("/quiz/{quizId}")
    public ApiResponse<List<QuestionResponse>> listByQuiz(@PathVariable Long quizId) {
        QuizEntity quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return ApiResponse.<List<QuestionResponse>>builder().message("Quiz not found").build();
        List<QuestionEntity> list = questionRepository.findByQuiz(quiz);
        List<QuestionResponse> result = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.<List<QuestionResponse>>builder().result(result).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionResponse> getById(@PathVariable Long id) {
        QuestionEntity q = questionRepository.findById(id).orElse(null);
        if (q == null) return ApiResponse.<QuestionResponse>builder().message("Question not found").build();
        return ApiResponse.<QuestionResponse>builder().result(toResponse(q)).build();
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionResponse> update(@PathVariable Long id, @RequestBody @Valid QuestionRequest request) {
        QuestionEntity q = questionRepository.findById(id).orElse(null);
        if (q == null) return ApiResponse.<QuestionResponse>builder().message("Question not found").build();
        if (request.getContent() != null) q.setContent(request.getContent());
        if (request.getAudioUrl() != null) q.setAudioUrl(request.getAudioUrl());
        if (request.getOptions() != null) q.setOptions(QuestionOptionUtils.optionsToJson(request.getOptions()));
        if (request.getCorrectOption() != null) q.setCorrectOption(QuestionOptionUtils.toLetter(request.getCorrectOption()));
        if (request.getExplanation() != null) q.setExplanation(request.getExplanation());
        if (request.getSkill() != null) q.setSkill(request.getSkill());
        QuestionEntity saved = questionRepository.save(q);
        return ApiResponse.<QuestionResponse>builder().result(toResponse(saved)).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        QuestionEntity q = questionRepository.findById(id).orElse(null);
        if (q == null) return ApiResponse.<Void>builder().message("Question not found").build();
        questionRepository.delete(q);
        return ApiResponse.<Void>builder().message("Question deleted").build();
    }

    // Helpers
    private QuestionResponse toResponse(QuestionEntity q) {
        List<String> options = null;
        if (q.getOptions() != null) options = QuestionOptionUtils.storedJsonToList(q.getOptions());
        return QuestionResponse.builder()
                .id(q.getId())
                .quizId(q.getQuiz() != null ? q.getQuiz().getId() : null)
                .content(q.getContent())
                .audioUrl(q.getAudioUrl())
                .options(options)
                .correctOption(QuestionOptionUtils.normalizeCorrectOption(q.getCorrectOption()))
                .explanation(q.getExplanation())
                .skill(q.getSkill())
                .build();
    }
}
