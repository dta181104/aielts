package com.example.shopmohinh.controller.course;

import com.example.shopmohinh.dto.request.QuizRequest;
import com.example.shopmohinh.dto.request.QuestionRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.QuestionResponse;
import com.example.shopmohinh.dto.response.QuizResponse;
import com.example.shopmohinh.entity.course.LessonEntity;
import com.example.shopmohinh.entity.course.QuestionEntity;
import com.example.shopmohinh.entity.course.QuizEntity;
import com.example.shopmohinh.repository.course.LessonRepository;
import com.example.shopmohinh.repository.QuestionRepository;
import com.example.shopmohinh.repository.QuizRepository;
import com.example.shopmohinh.util.QuestionOptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
@Slf4j
public class QuizAdminController {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;

    @GetMapping("/lesson/{lessonId}")
    public ApiResponse<List<QuizResponse>> getByLesson(@PathVariable Long lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) {
            return ApiResponse.<List<QuizResponse>>builder().message("Lesson not found").build();
        }
        List<QuizEntity> quizzes = quizRepository.findByLesson(lesson);
        List<QuizResponse> result = quizzes.stream()
                .map(q -> toQuizResponse(q, false))
                .collect(Collectors.toList());
        return ApiResponse.<List<QuizResponse>>builder().result(result).build();
    }

    // Added: handle GET /quizzes/{id} to return quiz with questions
    @GetMapping("/{id}")
    public ApiResponse<QuizResponse> getById(@PathVariable Long id) {
        QuizEntity quiz = quizRepository.findById(id).orElse(null);
        if (quiz == null) {
            return ApiResponse.<QuizResponse>builder().message("Quiz not found").build();
        }
        List<QuestionEntity> questions = questionRepository.findByQuiz(quiz);
        List<QuestionResponse> questionResponses = questions.stream().map(this::toQuestionResponse).collect(Collectors.toList());
        return ApiResponse.<QuizResponse>builder().result(toQuizResponse(quiz, questionResponses)).build();
    }

    @PutMapping("/{id}")
    public ApiResponse<QuizResponse> update(@PathVariable Long id, @RequestBody QuizRequest request) {
        QuizEntity quiz = quizRepository.findById(id).orElse(null);
        if (quiz == null) {
            return ApiResponse.<QuizResponse>builder().message("Quiz not found").build();
        }
        if (request.getTitle() != null) quiz.setTitle(request.getTitle());
        if (request.getPassScore() != null) quiz.setPassScore(request.getPassScore());
        if (request.getDuration() != null) quiz.setDuration(request.getDuration());
        // optionally allow moving to another lesson
        if (request.getLessonId() != null && !request.getLessonId().equals(quiz.getLesson().getId())) {
            LessonEntity lesson = lessonRepository.findById(request.getLessonId()).orElse(null);
            if (lesson != null) quiz.setLesson(lesson);
        }
        QuizEntity saved = quizRepository.save(quiz);
        // load questions
        List<QuestionEntity> questions = questionRepository.findByQuiz(saved);
        List<QuestionResponse> questionResponses = questions.stream().map(this::toQuestionResponse).collect(Collectors.toList());
        return ApiResponse.<QuizResponse>builder().result(toQuizResponse(saved, questionResponses)).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        QuizEntity quiz = quizRepository.findById(id).orElse(null);
        if (quiz == null) {
            return ApiResponse.<Void>builder().message("Quiz not found").build();
        }
        quizRepository.delete(quiz);
        return ApiResponse.<Void>builder().message("Quiz deleted").build();
    }

    @PostMapping("/{quizId}/questions")
    public ApiResponse<QuestionResponse> createQuestion(@PathVariable Long quizId, @RequestBody QuestionRequest request) {
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
        return ApiResponse.<QuestionResponse>builder().result(toQuestionResponse(saved)).build();
    }

    // Helpers
    private QuizResponse toQuizResponse(QuizEntity quiz, Object questionsObj) {
        List<QuestionResponse> questions;
        if (questionsObj == null) {
            questions = new ArrayList<>();
        } else if (questionsObj instanceof List) {
            questions = (List<QuestionResponse>) questionsObj;
        } else {
            questions = new ArrayList<>();
        }
        return QuizResponse.builder()
                .id(quiz.getId())
                .lessonId(quiz.getLesson() != null ? quiz.getLesson().getId() : null)
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
                .quizId(q.getQuiz() != null ? q.getQuiz().getId() : null)
                .content(q.getContent())
                .audioUrl(q.getAudioUrl())
                .options(options)
                .correctOption(correctOpt)
                .explanation(q.getExplanation())
                .skill(q.getSkill())
                .build();
    }

    private List<String> parseJsonArray(String json) {
        String trimmed = json == null ? "" : json.trim();
        if (trimmed.length() < 2) return new ArrayList<>();
        String inner = trimmed.substring(1, trimmed.length() - 1);
        if (inner.isEmpty()) return new ArrayList<>();
        return Arrays.stream(inner.split(","))
                .map(s -> s.trim())
                .map(s -> s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s)
                .collect(Collectors.toList());
    }
}
