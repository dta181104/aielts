package com.example.shopmohinh.dto.request;

import jakarta.validation.constraints.Min;
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
public class QuizRequest {
    @NotNull(message = "LESSON_ID_REQUIRED")
    Long lessonId;

    @NotBlank(message = "TITLE_REQUIRED")
    @Size(max = 255, message = "TITLE_TOO_LONG")
    String title;

    @Min(value = 0, message = "PASS_SCORE_INVALID")
    Integer passScore; // percent needed to pass

    @Min(value = 0, message = "DURATION_INVALID")
    Integer duration; // minutes
}
