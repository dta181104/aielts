package com.example.shopmohinh.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageRequest {
    MultipartFile imageFile;

    String imageUrl;

    Boolean mainImage;
}
