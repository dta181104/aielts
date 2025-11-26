package com.example.shopmohinh.dto.request;

import com.example.shopmohinh.validation.ValidCorrectOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidCorrectOption
public class QuestionRequest {

    @NotBlank(message = "Content must not be blank")
    private String content;

    private String audioUrl;

    @Size(min = 1, message = "At least one option is required")
    private List<@NotBlank(message = "Option must not be blank") String> options;

    @NotNull(message = "Correct option index is required")
    private Integer correctOption;

    private String explanation;

    private String skill;

    // Optional: associate question to existing quiz or lesson (one of them can be provided)
    private Long quizId;
    private Long lessonId;
}
