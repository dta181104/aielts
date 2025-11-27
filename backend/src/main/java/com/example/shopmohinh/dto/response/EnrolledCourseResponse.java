package com.example.shopmohinh.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnrolledCourseResponse {
    Long id;
    String title;
    String thumbnail;
    Integer progressPercent;
    LocalDateTime enrolledDate;
    String status;
}

