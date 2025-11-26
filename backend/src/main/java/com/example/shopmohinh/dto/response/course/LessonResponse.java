package com.example.shopmohinh.dto.response.course;

import com.example.shopmohinh.entity.course.LessonType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonResponse {

    Long id;
    Long sectionId;
    String title;
    LessonType type;
    String videoUrl;
    String content;
    Integer duration;
    Integer orderIndex;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
    Boolean deleted;
    List<AttachmentResponse> attachments;
}

