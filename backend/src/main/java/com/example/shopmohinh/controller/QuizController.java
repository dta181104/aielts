package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.QuestionRequest;
import com.example.shopmohinh.dto.request.QuizRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.QuestionResponse;
import com.example.shopmohinh.dto.response.QuizResponse;
import com.example.shopmohinh.service.impl.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ApiResponse<QuizResponse> createQuiz(@RequestBody @Valid QuizRequest request) {
        return ApiResponse.<QuizResponse>builder()
                .result(quizService.createQuiz(request))
                .build();
    }

    @PostMapping("/question")
    public ApiResponse<QuestionResponse> addQuestion(@RequestBody @Valid QuestionRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(quizService.addQuestion(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<QuizResponse> getQuiz(@PathVariable Long id) {
        return ApiResponse.<QuizResponse>builder()
                .result(quizService.getQuiz(id))
                .build();
    }
}
