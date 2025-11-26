package com.example.shopmohinh.dto.request.course;

import com.example.shopmohinh.entity.course.CourseStatus;
import com.example.shopmohinh.entity.course.CourseType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseRequest {

    @NotBlank(message = "Title không được để trống")
    @Size(max = 255, message = "Title không được quá 255 ký tự")
    String title;

    @Size(max = 100, message = "Level name không được quá 100 ký tự")
    String levelName;

    @Min(value = 0, message = "Target band phải >= 0")
    @Max(value = 9, message = "Target band phải <= 9")
    Float targetBand;

    @DecimalMin(value = "0.0", message = "Price phải >= 0")
    BigDecimal price;

    @Size(max = 500, message = "Thumbnail URL không được quá 500 ký tự")
    String thumbnail;

    String description;

    CourseType courseType;

    CourseStatus status;
}

