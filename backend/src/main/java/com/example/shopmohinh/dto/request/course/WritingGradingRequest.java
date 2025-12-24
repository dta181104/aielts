package com.example.shopmohinh.dto.request.course;

import lombok.Data;

@Data
public class WritingGradingRequest {
    private Integer section;
    private String writingTopic;
    private String writingAnswer;
}
