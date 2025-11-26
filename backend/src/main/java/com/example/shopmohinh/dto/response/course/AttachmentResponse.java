package com.example.shopmohinh.dto.response.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttachmentResponse {

    Long id;
    Long lessonId;
    String name;
    String url;
    String fileType;
}

