package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.ProductRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.ProductResponse;
import com.example.shopmohinh.dto.search.ProductSearch;
import com.example.shopmohinh.service.impl.ProductService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    @Autowired
    ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> create(@RequestBody ProductRequest request){
        return ApiResponse.<ProductResponse>builder()
                .result(productService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<ProductResponse>> getAll(@NonNull ProductSearch request){
        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.getProduct(request))
                .build();
    }
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(@PathVariable Long id){
        return ApiResponse.<ProductResponse>builder()
                .code(1000)
                .result(productService.getDetailProduct(id))
                .build();
    }


    @DeleteMapping("/{code}")
    public ApiResponse<ProductResponse> delete(@PathVariable("code") String code){
        productService.delete(code);
        return ApiResponse.<ProductResponse>builder().build();
    }

    @PutMapping("/{code}")
    public ApiResponse<ProductResponse> updateUser(@PathVariable("code") String code,
                                                @RequestBody ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.update(code,request))
                .build();

    }
}
