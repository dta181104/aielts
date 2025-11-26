package com.example.shopmohinh.controller.course;

import com.example.shopmohinh.dto.request.course.AttachmentRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.course.AttachmentResponse;
import com.example.shopmohinh.service.course.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    public ApiResponse<AttachmentResponse> create(@RequestBody @Valid AttachmentRequest request) {
        return ApiResponse.<AttachmentResponse>builder()
                .result(attachmentService.create(request))
                .build();
    }

    @GetMapping("/lesson/{lessonId}")
    public ApiResponse<List<AttachmentResponse>> getByLessonId(@PathVariable Long lessonId) {
        return ApiResponse.<List<AttachmentResponse>>builder()
                .result(attachmentService.getByLessonId(lessonId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AttachmentResponse> getById(@PathVariable Long id) {
        return ApiResponse.<AttachmentResponse>builder()
                .result(attachmentService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<AttachmentResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid AttachmentRequest request) {
        return ApiResponse.<AttachmentResponse>builder()
                .result(attachmentService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        attachmentService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Attachment deleted successfully")
                .build();
    }
}

