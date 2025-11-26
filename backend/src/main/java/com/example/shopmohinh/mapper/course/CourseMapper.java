package com.example.shopmohinh.mapper.course;

import com.example.shopmohinh.dto.request.course.CourseRequest;
import com.example.shopmohinh.dto.response.course.CourseResponse;
import com.example.shopmohinh.entity.course.CourseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "sections", ignore = true)
    CourseEntity toEntity(CourseRequest request);

    CourseResponse toResponse(CourseEntity entity);

    @Mapping(target = "sections", ignore = true)
    void updateEntity(@MappingTarget CourseEntity entity, CourseRequest request);
}

