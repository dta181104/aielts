package com.example.shopmohinh.service;

import com.example.shopmohinh.dto.request.CartDetailRequest;
import com.example.shopmohinh.dto.response.CartDetailResponse;

import java.util.List;

public interface CartDetailService {
    CartDetailResponse addProductToCartDetail(CartDetailRequest request);

    String deleteProduct(List<Long> productIds);
}
