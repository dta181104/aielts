package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.CourseQuestionRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.QuestionResponse;
import com.example.shopmohinh.service.impl.CourseQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseQuestionController {

    private final CourseQuestionService courseQuestionService;

    @PostMapping("/{courseId}/lesson/{lessonId}/question")
    public ApiResponse<QuestionResponse> addQuestion(@PathVariable Long courseId,
                                                     @PathVariable Long lessonId,
                                                     @RequestBody CourseQuestionRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(courseQuestionService.addQuestion(courseId, lessonId, request))
                .build();
    }
}

