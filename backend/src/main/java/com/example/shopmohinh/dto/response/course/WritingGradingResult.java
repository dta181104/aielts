package com.example.shopmohinh.dto.response.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WritingGradingResult {

    private Double overallBand;
    // Chỉ có 1 trong 2 mục taScore hoặc trScore tương ứng với section 1 và 2
    private Double taScore; // Task Achievement
    private Double trScore; // Task Response

    private Double ccScore; // Coherence and Cohesion
    private Double lrScore; // Lexical Resource
    private Double graScore; // Grammatical Range and Accuracy

    private String feedback;

}