package com.example.shopmohinh.dto.response.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SectionResponse {

    Long id;
    Long courseId;
    Long categoryId;
    String categoryName;
    String title;
    Integer orderIndex;
    Boolean deleted;
    List<LessonResponse> lessons;
}

