package com.example.shopmohinh.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizResponse {
    Long id;
    Long lessonId;
    String title;
    Integer passScore;
    Integer duration;
    List<QuestionResponse> questions;
}

