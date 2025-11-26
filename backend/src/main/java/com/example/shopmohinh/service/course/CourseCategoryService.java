package com.example.shopmohinh.service.course;

import com.example.shopmohinh.dto.request.course.CourseCategoryRequest;
import com.example.shopmohinh.dto.response.course.CourseCategoryResponse;
import com.example.shopmohinh.entity.course.CategoryEntity;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.course.CourseCategoryMapper;
import com.example.shopmohinh.repository.course.CourseCategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseCategoryService {

    CourseCategoryRepository categoryRepository;
    CourseCategoryMapper categoryMapper;

    @Transactional
    public CourseCategoryResponse create(CourseCategoryRequest request) {
        if (categoryRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.USER_EXISTED); // Reuse existing error or create CATEGORY_EXISTED
        }

        CategoryEntity category = categoryMapper.toEntity(request);
        CategoryEntity savedCategory = categoryRepository.save(category);

        log.info("Created category with code: {}", savedCategory.getCode());
        return categoryMapper.toResponse(savedCategory);
    }

    public List<CourseCategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CourseCategoryResponse getById(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return categoryMapper.toResponse(category);
    }

    public CourseCategoryResponse getByCode(String code) {
        CategoryEntity category = categoryRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CourseCategoryResponse update(Long id, CourseCategoryRequest request) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        categoryMapper.updateEntity(category, request);
        CategoryEntity updatedCategory = categoryRepository.save(category);

        log.info("Updated category with id: {}", id);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Transactional
    public void delete(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        categoryRepository.delete(category);
        log.info("Deleted category with id: {}", id);
    }
}

