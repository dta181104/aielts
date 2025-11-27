package com.example.shopmohinh.controller.identity;

import com.example.shopmohinh.dto.request.course.CourseRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.course.CourseResponse;
import com.example.shopmohinh.entity.course.CourseStatus;
import com.example.shopmohinh.entity.course.CourseType;
import com.example.shopmohinh.service.identity.IdentityCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/identity/courses")
@RequiredArgsConstructor
public class IdentityCourseController {

    private final IdentityCourseService identityCourseService;

    @GetMapping
    public ApiResponse<Page<CourseResponse>> getAll(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "id") String sortBy,
                                                    @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return identityCourseService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getById(@PathVariable Long id) {
        return identityCourseService.getById(id);
    }

    @GetMapping("/status/{status}")
    public ApiResponse<Page<CourseResponse>> getByStatus(@PathVariable CourseStatus status,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return identityCourseService.getByStatus(status, pageable);
    }

    @GetMapping("/type/{type}")
    public ApiResponse<Page<CourseResponse>> getByType(@PathVariable CourseType type,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return identityCourseService.getByType(type, pageable);
    }

    @GetMapping("/search")
    public ApiResponse<Page<CourseResponse>> search(@RequestParam(required = false) CourseStatus status,
                                                    @RequestParam(required = false) CourseType type,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return identityCourseService.search(status, type, keyword, pageable);
    }

    @PostMapping
    public ApiResponse<CourseResponse> create(@RequestBody @Valid CourseRequest request) {
        return identityCourseService.create(request);
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseResponse> update(@PathVariable Long id, @RequestBody @Valid CourseRequest request) {
        return identityCourseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        return identityCourseService.delete(id);
    }
}
