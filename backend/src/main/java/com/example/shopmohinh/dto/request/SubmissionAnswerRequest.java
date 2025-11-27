package com.example.shopmohinh.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmissionAnswerRequest {
    @NotNull(message = "questionId is required")
    Long questionId;

    String selectedOption; // A/B/C etc
    String textAnswer; // for writing
    String audioUrl; // for speaking
}

