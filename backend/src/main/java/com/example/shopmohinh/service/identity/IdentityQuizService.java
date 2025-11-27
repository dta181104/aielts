package com.example.shopmohinh.service.identity;

import com.example.shopmohinh.dto.request.CourseQuestionRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.QuestionResponse;
import com.example.shopmohinh.service.impl.CourseQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IdentityQuizService {

    private final CourseQuestionService courseQuestionService;

    public ApiResponse<List<QuestionResponse>> listQuestions(Long quizId) {
        return ApiResponse.<List<QuestionResponse>>builder()
                .result(courseQuestionService.listQuestions(quizId))
                .build();
    }

    public ApiResponse<QuestionResponse> getQuestion(Long questionId) {
        return ApiResponse.<QuestionResponse>builder()
                .result(courseQuestionService.getQuestion(questionId))
                .build();
    }

    public ApiResponse<QuestionResponse> createQuestion(Long quizId, CourseQuestionRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(courseQuestionService.createQuestion(quizId, request))
                .build();
    }

    public ApiResponse<QuestionResponse> updateQuestion(Long questionId, CourseQuestionRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(courseQuestionService.updateQuestion(questionId, request))
                .build();
    }

    public ApiResponse<Void> deleteQuestion(Long questionId) {
        courseQuestionService.deleteQuestion(questionId);
        return ApiResponse.<Void>builder().build();
    }
}
