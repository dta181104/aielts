package com.example.shopmohinh.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmissionAnswerResponse {
    Long id;
    Long questionId;
    String selectedOption;
    Boolean isCorrect;
    String textAnswer;
    String audioUrl;
    BigDecimal gradeScore;
    String teacherNote;
}
