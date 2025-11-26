package com.example.shopmohinh.dto.response.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseCategoryResponse {

    Long id;
    String code;
    String name;
    String description;
}

