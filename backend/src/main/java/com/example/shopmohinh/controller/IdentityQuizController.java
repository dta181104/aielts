package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.QuizRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.QuizResponse;
import com.example.shopmohinh.service.impl.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// Commented out to avoid duplicate request mapping with QuizAdminController
// @RestController
// @RequestMapping("/quizzes")
@RequiredArgsConstructor
public class IdentityQuizController {

    private final QuizService quizService;

    public ApiResponse<QuizResponse> getById(Long id) {
        QuizResponse resp = quizService.getQuiz(id);
        return ApiResponse.<QuizResponse>builder().result(resp).build();
    }

    public ApiResponse<QuizResponse> create(@Valid QuizRequest request) {
        QuizResponse resp = quizService.createQuiz(request);
        return ApiResponse.<QuizResponse>builder().result(resp).build();
    }
}
