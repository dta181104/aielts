package com.example.shopmohinh.dto.response;

import com.example.shopmohinh.entity.ImageEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageResponse {
    Long idImage;

    String imageUrl;

    boolean isImageMain;

    public ImageResponse(ImageEntity imageEntity) {
        this.idImage = imageEntity.getId();
        this.imageUrl = imageEntity.getImageUrl();
        this.isImageMain = imageEntity.isMainImage();
    }
}
