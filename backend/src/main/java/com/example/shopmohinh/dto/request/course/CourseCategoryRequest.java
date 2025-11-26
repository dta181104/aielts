package com.example.shopmohinh.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseCategoryRequest {

    @NotBlank(message = "Code không được để trống")
    @Size(max = 50, message = "Code không được quá 50 ký tự")
    String code;

    @NotBlank(message = "Name không được để trống")
    @Size(max = 255, message = "Name không được quá 255 ký tự")
    String name;

    String description;
}

