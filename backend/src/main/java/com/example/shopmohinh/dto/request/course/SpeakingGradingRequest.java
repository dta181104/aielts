package com.example.shopmohinh.dto.request.course;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SpeakingGradingRequest {
    private Integer section;
    private String speakingTopic;
    private MultipartFile audioFile;
}
