package com.example.shopmohinh.mapper;

import com.example.shopmohinh.dto.response.CartDetailResponse;
import com.example.shopmohinh.entity.CartDetailEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartDetailMapper {
    CartDetailResponse toResponse(CartDetailEntity cartDetailEntity);
}
