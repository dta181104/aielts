package com.example.shopmohinh.controller;

import com.example.shopmohinh.dto.request.CartDetailRequest;
import com.example.shopmohinh.dto.response.ApiResponse;
import com.example.shopmohinh.dto.response.CartDetailResponse;
import com.example.shopmohinh.service.impl.CartDetailServiceImpl;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart-detail")
@Slf4j
@RequiredArgsConstructor
public class CartDetailController {
    private final CartDetailServiceImpl cartDetailService;

    @PostMapping
    public ApiResponse<CartDetailResponse> addProductToCartDetail(@RequestBody CartDetailRequest request) {
        return ApiResponse.<CartDetailResponse>builder()
                .code(1000)
                .result(cartDetailService.addProductToCartDetail(request))
                .build();
    }

    @DeleteMapping
    public ApiResponse<String> deleteProduct(List<Long> productIds) {
        return ApiResponse.<String>builder()
                .code(1000)
                .result(cartDetailService.deleteProduct(productIds))
                .build();
    }
}
