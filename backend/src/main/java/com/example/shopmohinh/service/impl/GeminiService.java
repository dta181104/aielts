package com.example.shopmohinh.service.impl;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.shopmohinh.dto.response.course.WritingGradingResult;
import com.google.genai.types.Type;
import com.google.genai.types.Schema;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @SuppressWarnings("unused")
//    @Value("${gemini.api.key}")
//    private String geminiApiKey;
    @Value("${gemini.api.model}")
    private String geminiApiModel;

//    private Client client;
    private final Client client;
    private final ObjectMapper objectMapper = new ObjectMapper();

//    @PostConstruct
//    public void init() {
//        this.client = Client.builder()
//                .apiKey(geminiApiKey)
//                .build();
//    }

    public WritingGradingResult gradeWriting(Integer section, String essayTopic, String essayContent) throws IOException {

        String systemInstruction = "Bạn là giám khảo IELTS Writing. BẮT BUỘC phải trả lời bằng MỘT JSON hợp lệ, không có bất kỳ văn bản giải thích nào khác ngoài JSON.";
        String promptText = buildWritingPrompt(section, essayTopic, essayContent);
        System.out.println(promptText);

        Content content = Content.fromParts(Part.fromText(promptText));
        String firstScoreStr = (section == 1) ? "taScore" : "trScore";
        String firstFeedbackStr = (section == 1) ? "taFeedback" : "trFeedback";

        Schema responseSchema =
                Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(ImmutableMap.of(
                                "overallBand", Schema.builder().type(Type.Known.NUMBER).build(),
                                firstScoreStr, Schema.builder().type(Type.Known.NUMBER).build(),
                                "ccScore", Schema.builder().type(Type.Known.NUMBER).build(),
                                "lrScore", Schema.builder().type(Type.Known.NUMBER).build(),
                                "graScore", Schema.builder().type(Type.Known.NUMBER).build(),
                                "feedback", Schema.builder().type(Type.Known.OBJECT)
                                        .properties(ImmutableMap.of(
                                                "generalFeedback", Schema.builder().type(Type.Known.STRING).build(),
                                                "strongPoints", Schema.builder().type(Type.Known.STRING).build(),
                                                "weakPoints", Schema.builder().type(Type.Known.STRING).build(),
                                                firstFeedbackStr, Schema.builder().type(Type.Known.STRING).build(),
                                                "ccFeedback", Schema.builder().type(Type.Known.STRING).build(),
                                                "lrFeedback", Schema.builder().type(Type.Known.STRING).build(),
                                                "graFeedback", Schema.builder().type(Type.Known.STRING).build()
                                        ))
                                        .required(ImmutableList.of("generalFeedback", "strongPoints", "weakPoints", firstFeedbackStr, "ccFeedback", "lrFeedback", "graFeedback"))
                                        .build()
                        ))
                        .required(ImmutableList.of("overallBand", firstScoreStr, "ccScore", "lrScore", "graScore", "feedback"))
                        .build();


        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .responseSchema(responseSchema)
                        .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
                        .candidateCount(1)
                        .build();

        try {
            GenerateContentResponse response = client.models.generateContent(
                    geminiApiModel,
                    content,
                    config
            );

            // Read raw response first and check for null/blank before any string operations
            String rawResult = response.text();
            if (rawResult == null || rawResult.isBlank()) {
                throw new RuntimeException("Gemini API không trả về kết quả.");
            }
            System.out.println("Gemini API trả về kết quả: " + rawResult);

            return objectMapper.readValue(rawResult, WritingGradingResult.class);

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API Gemini: " + e.getMessage());
            throw new IOException("Không thể chấm điểm Writing qua Gemini.", e);
        }
    }

    private String buildWritingPrompt(Integer section, String topic, String essay) {
        // 1. Xác định tiêu chí chấm dựa trên Task 1 hay Task 2
        String taskCriteria = (section == 1) ? "Task Achievement" : "Task Response";
        String firstScoreStr = (section == 1) ? "taScore" : "trScore";

        // 2. Thêm chỉ dẫn về định dạng Feedback để FE hiển thị đẹp hơn
        return String.format("""
    Yêu cầu đề bài IELTS Writing Task %d:
    %s
    ---
    Nội dung bài viết của thí sinh:
    %s
    ---
    NHIỆM VỤ CỦA BẠN:
    Đánh giá bài viết dựa trên 4 tiêu chí chấm thi IELTS chuẩn:
    1. %s (%s) - Lưu ý: Đây là Task %d
    2. Coherence and Cohesion (ccScore)
    3. Lexical Resource (lrScore)
    4. Grammatical Range and Accuracy (graScore)
    
    YÊU CẦU ĐẦU RA:
    1. Điểm số: Chấm từng tiêu chí thang điểm 1.0 - 9.0 (bước nhảy 0.5).
    2. Overall Band: Tính trung bình cộng 4 tiêu chí (làm tròn theo quy tắc IELTS: .25 lên .5, .75 lên 1.0).
    3. Feedback (Quan trọng):
       - Phải viết bằng TIẾNG VIỆT, giọng văn xây dựng, khuyến khích.
       - Cấu trúc bắt buộc trong feedback:
         + Nhận xét chung: ...
         + Điểm mạnh: ...
         + Điểm yếu: (Chỉ ra lỗi sai cụ thể trong bài nếu có và cách sửa)
         + Nhận xét 4 tiêu chí
    """,
                section, topic, essay, taskCriteria, firstScoreStr, section);
    }

}