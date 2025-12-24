package com.example.shopmohinh.dto.response.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingGradingResponse {

    private Double overallBand;
    private Double fcScore; // Fluency and Coherence
    private Double lrScore; // Lexical Resource
    private Double graScore; // Grammatical Range and Accuracy
    private Double prScore;  // Pronunciation

    private Feedback feedback;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Feedback {
        private String yourSpeech;
        private String generalFeedback;
        private String strongPoints;
        private String weakPoints;
        private String fcFeedback;
        private String lrFeedback;
        private String graFeedback;
        private String prFeedback;
    }
}
