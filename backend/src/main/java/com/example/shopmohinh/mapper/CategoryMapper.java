package com.example.shopmohinh.mapper;

import com.example.shopmohinh.dto.request.CategoryRequest;
import com.example.shopmohinh.dto.request.RoleRequest;
import com.example.shopmohinh.dto.response.CategoryResponse;
import com.example.shopmohinh.dto.response.RoleResponse;
import com.example.shopmohinh.entity.Category;
import com.example.shopmohinh.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

//báo cho Maptruct bt class này để sd trong spring
@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequest request);

    CategoryResponse toCategoryResponse(Category category);

    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}
