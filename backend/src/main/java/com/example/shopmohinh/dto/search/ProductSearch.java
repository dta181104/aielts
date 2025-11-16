package com.example.shopmohinh.dto.search;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductSearch extends SearchDto {
    String code = null;

    String name = null;

    int status;

    Double height;

    Double weight;

    BigDecimal price;
}
