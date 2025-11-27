package com.example.shopmohinh.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizSubmissionResponse {
    Long id;
    Long userId;
    Long quizId;
    LocalDateTime startTime;
    LocalDateTime submitTime;
    BigDecimal score;
    String status;
    String teacherFeedback;
    List<SubmissionAnswerResponse> answers;
}
