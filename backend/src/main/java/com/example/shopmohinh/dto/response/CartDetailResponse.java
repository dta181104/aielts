package com.example.shopmohinh.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartDetailResponse {
    Long id;

    String code;

    Integer quantity;

    ProductResponse productResponse;

    String cartCode;

    String username;


}
