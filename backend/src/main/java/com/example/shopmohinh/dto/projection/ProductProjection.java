package com.example.shopmohinh.dto.projection;

import java.math.BigDecimal;

public interface ProductProjection {
    Long getId();
    Long getIdImage();
    String getCode();
    String getName();
    String getDescription();
    double getHeight();
    BigDecimal getPrice();
    Integer getQuantity();
    int getStatus();
    double getWeight();
    boolean isMainImage();
    String getImageUrl();
}
