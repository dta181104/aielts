package com.example.shopmohinh.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResponse {
    Long id;
    Long quizId;
    String content;
    String audioUrl;
    List<String> options;
    String correctOption;
    String explanation;
    String skill;
}
