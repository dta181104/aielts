package com.example.shopmohinh.mapper;

import com.example.shopmohinh.dto.projection.ProductProjection;
import com.example.shopmohinh.dto.request.ProductRequest;
import com.example.shopmohinh.dto.response.ProductResponse;
import com.example.shopmohinh.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

//báo cho Maptruct bt class này để sd trong spring
@Mapper(componentModel = "spring")
public interface ProductMapper {
//    @Mapping(target = "permissions",ignore = true)
    Product toProduct(ProductRequest request);

    ProductResponse toProductResponse(Product request);

    ProductResponse toProductResponse(ProductProjection projection);

//    @Mapping(target = "permissions",ignore = true)
    void updateProduct(@MappingTarget Product product, ProductRequest request);
}
