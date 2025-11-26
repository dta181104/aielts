package com.example.shopmohinh.mapper.course;

import com.example.shopmohinh.dto.request.course.CourseCategoryRequest;
import com.example.shopmohinh.dto.response.course.CourseCategoryResponse;
import com.example.shopmohinh.entity.course.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CourseCategoryMapper {

    CategoryEntity toEntity(CourseCategoryRequest request);

    CourseCategoryResponse toResponse(CategoryEntity entity);

    void updateEntity(@MappingTarget CategoryEntity entity, CourseCategoryRequest request);
}

