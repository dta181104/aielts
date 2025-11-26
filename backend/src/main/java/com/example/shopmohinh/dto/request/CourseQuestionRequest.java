package com.example.shopmohinh.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseQuestionRequest {
    String content;
    String audioUrl;
    List<String> options;
    // Accept letter like "A" or numeric string
    String correctOption;
    String explanation;
    String skill;
}
