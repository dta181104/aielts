package com.example.shopmohinh.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SectionRequest {

    @NotNull(message = "Course ID không được để trống")
    Long courseId;

    Long categoryId;

    @NotBlank(message = "Title không được để trống")
    @Size(max = 255, message = "Title không được quá 255 ký tự")
    String title;

    Integer orderIndex;
}

