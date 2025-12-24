package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.course.WritingGradingRequest;
import com.example.shopmohinh.dto.response.course.WritingGradingResponse;
import com.example.shopmohinh.dto.request.course.SpeakingGradingRequest;
import com.example.shopmohinh.dto.response.course.SpeakingGradingResponse;
import com.example.shopmohinh.service.impl.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequestMapping("/gemini")
@RestController
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @PostMapping("/grading/writing")
    public ResponseEntity<?> geminiGradingWriting(@RequestBody WritingGradingRequest request) {
        try {
            WritingGradingResponse result = geminiService.gradeWriting(
                    request.getSection(),
                    request.getWritingTopic(),
                    request.getWritingContent()
            );

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().body("Lỗi hệ thống chấm điểm: " + e.getMessage());
        }
    }

    @PostMapping("/grading/speaking")
    public ResponseEntity<?> geminiGradingSpeaking(@RequestBody SpeakingGradingRequest request) {
        try {
            SpeakingGradingResponse result = geminiService.gradeSpeaking(
                    request.getSection(),
                    request.getSpeakingTopic(),
                    request.getAudioFile()
            );

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().body("Lỗi hệ thống chấm điểm: " + e.getMessage());
        }
    }
}
