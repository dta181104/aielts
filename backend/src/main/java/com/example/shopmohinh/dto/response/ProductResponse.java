package com.example.shopmohinh.dto.response;

import com.example.shopmohinh.dto.projection.ProductProjection;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;

    String code;

    String name;

    String description;

    int status;

    Double height;

    Double weight;

    Integer quantity;

    BigDecimal price;

    Long category_id;

    Boolean deleted;

    LocalDateTime createdDate;

    LocalDateTime updatedDate;

    String createdBy;

    String updatedBy;

    List<ImageResponse> images;

    public ProductResponse(ProductProjection productProjection) {
        this.id = productProjection.getId();
        this.code = productProjection.getCode();
        this.name = productProjection.getName();
        this.description = productProjection.getDescription();
        this.status = productProjection.getStatus();
        this.height = productProjection.getHeight();
        this.weight = productProjection.getWeight();
        this.quantity = productProjection.getQuantity();
        this.price = productProjection.getPrice();
        this.images(productProjection);
    }


    private void images(ProductProjection productProjection) {
        if (productProjection.getImageUrl() != null) {
            this.images = Collections.singletonList(
                    ImageResponse.builder()
                            .idImage(productProjection.getIdImage())
                            .imageUrl(productProjection.getImageUrl())
                            .isImageMain(productProjection.isMainImage())
                            .build()
            );
        } else {
            this.images = Collections.emptyList();
        }
    }

}
