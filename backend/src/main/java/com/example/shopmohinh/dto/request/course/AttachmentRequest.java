package com.example.shopmohinh.dto.request.course;

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
public class AttachmentRequest {

    @NotNull(message = "Lesson ID không được để trống")
    Long lessonId;

    @Size(max = 255, message = "Name không được quá 255 ký tự")
    String name;

    @NotBlank(message = "URL không được để trống")
    @Size(max = 500, message = "URL không được quá 500 ký tự")
    String url;

    @Size(max = 20, message = "File type không được quá 20 ký tự")
    String fileType;
}

