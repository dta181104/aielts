package com.example.shopmohinh.dto.request.course;

import com.example.shopmohinh.entity.course.LessonType;
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
public class LessonRequest {

    @NotNull(message = "Section ID không được để trống")
    Long sectionId;

    @NotBlank(message = "Title không được để trống")
    @Size(max = 255, message = "Title không được quá 255 ký tự")
    String title;

    @NotNull(message = "Type không được để trống")
    LessonType type;

    @Size(max = 500, message = "Video URL không được quá 500 ký tự")
    String videoUrl;

    String content;

    Integer duration;

    Integer orderIndex;
}

