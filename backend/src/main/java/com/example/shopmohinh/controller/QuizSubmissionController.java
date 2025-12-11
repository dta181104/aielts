package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.SubmissionAnswerRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.QuizSubmissionResponse;
import com.example.shopmohinh.dto.response.SubmissionAnswerResponse;
import com.example.shopmohinh.service.impl.QuizSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/quizzes")
public class QuizSubmissionController {

    private final QuizSubmissionService submissionService;

    public QuizSubmissionController(QuizSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/{quizId}/submissions")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> startSubmission(@PathVariable Long quizId, @RequestParam(required = false) Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        QuizSubmissionResponse resp = submissionService.startSubmission(quizId, userId);
        return ResponseEntity.ok(ApiResponse.<QuizSubmissionResponse>builder().code(1000).message("OK").result(resp).build());
    }

    @PostMapping("/submissions/{submissionId}/answers")
    public ResponseEntity<ApiResponse<SubmissionAnswerResponse>> addAnswer(@PathVariable Long submissionId, @Valid @RequestBody SubmissionAnswerRequest request) {
        SubmissionAnswerResponse resp = submissionService.addOrUpdateAnswer(submissionId, request);
        return ResponseEntity.ok(ApiResponse.<SubmissionAnswerResponse>builder().code(1000).message("OK").result(resp).build());
    }

    @PostMapping(value = "/submissions/{submissionId}/audio-answer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SubmissionAnswerResponse>> submitAudioAnswer(
            @PathVariable Long submissionId,
            @RequestPart("audioFile") MultipartFile audioFile,
            @RequestParam Long questionId) {

        SubmissionAnswerRequest req = SubmissionAnswerRequest.builder()
                .questionId(questionId)
                .audioFile(audioFile)
                .build();

        SubmissionAnswerResponse resp = submissionService.addOrUpdateAnswer(submissionId, req);
        return ResponseEntity.ok(ApiResponse.<SubmissionAnswerResponse>builder().code(1000).message("OK").result(resp).build());
    }

    @PutMapping("/submissions/{submissionId}/submit")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> submit(@PathVariable Long submissionId) {
        QuizSubmissionResponse resp = submissionService.submit(submissionId);
        return ResponseEntity.ok(ApiResponse.<QuizSubmissionResponse>builder().code(1000).message("OK").result(resp).build());
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> getSubmission(@PathVariable Long submissionId) {
        QuizSubmissionResponse resp = submissionService.getSubmission(submissionId);
        return ResponseEntity.ok(ApiResponse.<QuizSubmissionResponse>builder().code(1000).message("OK").result(resp).build());
    }
}
