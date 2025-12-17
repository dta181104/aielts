package com.example.shopmohinh.dto.request.course;

import lombok.Data;

@Data
public class WritingGradingRequest {
    private Integer section;
    private String essayTopic;
    private String essayContent;
}
