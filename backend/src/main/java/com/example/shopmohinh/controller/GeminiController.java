package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.course.WritingGradingRequest;
import com.example.shopmohinh.dto.response.course.WritingGradingResult;
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

    @PostMapping("/grading")
    public ResponseEntity<?> geminiGrading(@RequestBody WritingGradingRequest request) {
        try {
            WritingGradingResult result = geminiService.gradeWriting(
                    request.getSection(),
                    request.getEssayTopic(),
                    request.getEssayContent()
            );

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().body("Lỗi hệ thống chấm điểm: " + e.getMessage());
        }
    }
}
