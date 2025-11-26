package com.example.shopmohinh.controller.course;

import com.example.shopmohinh.dto.request.course.LessonRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.course.LessonResponse;
import com.example.shopmohinh.entity.course.LessonType;
import com.example.shopmohinh.service.course.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
@Slf4j
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    public ApiResponse<LessonResponse> create(@RequestBody @Valid LessonRequest request) {
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.create(request))
                .build();
    }

    @GetMapping("/section/{sectionId}")
    public ApiResponse<List<LessonResponse>> getBySectionId(@PathVariable Long sectionId) {
        return ApiResponse.<List<LessonResponse>>builder()
                .result(lessonService.getBySectionId(sectionId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<LessonResponse> getById(@PathVariable Long id) {
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.getById(id))
                .build();
    }

    @GetMapping("/section/{sectionId}/type/{type}")
    public ApiResponse<List<LessonResponse>> getBySectionIdAndType(
            @PathVariable Long sectionId,
            @PathVariable LessonType type) {
        return ApiResponse.<List<LessonResponse>>builder()
                .result(lessonService.getBySectionIdAndType(sectionId, type))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<LessonResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid LessonRequest request) {
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        lessonService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Lesson deleted successfully")
                .build();
    }
}

