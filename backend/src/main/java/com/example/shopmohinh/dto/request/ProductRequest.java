package com.example.shopmohinh.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {

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

    List<ImageRequest> images;

    LocalDateTime createdDate;

    LocalDateTime updatedDate;

    String createdBy;

    String updatedBy;
}
