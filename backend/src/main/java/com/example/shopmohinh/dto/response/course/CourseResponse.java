package com.example.shopmohinh.dto.response.course;

import com.example.shopmohinh.entity.course.CourseStatus;
import com.example.shopmohinh.entity.course.CourseType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseResponse {

    Long id;
    String title;
    String levelName;
    Float targetBand;
    BigDecimal price;
    String thumbnail;
    String description;
    CourseType courseType;
    CourseStatus status;
    LocalDateTime createdDate;
    Boolean deleted;
    List<SectionResponse> sections;
}

