package com.example.shopmohinh.mapper;

import com.example.shopmohinh.dto.request.BrandRequest;
import com.example.shopmohinh.dto.response.BrandResponse;
import com.example.shopmohinh.entity.BrandEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    BrandEntity toBrand(BrandRequest request);

    void updateBrand(@MappingTarget BrandEntity brand, BrandRequest request);

    BrandResponse toBrandResponse(BrandEntity brand);
}
