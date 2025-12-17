package com.example.shopmohinh.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;


//@Component // comment -> ko tự chạy
public class GenerateContentWithTextInput implements CommandLineRunner {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Override
    public void run(String... args) throws Exception {
        Client client = Client.builder()
                .apiKey(geminiApiKey)
                .build();

//        ImmutableMap<String, Object> schema = ImmutableMap.of(
//                "type", "object",
//                "properties", ImmutableMap.of(
//                        "recipe_name", ImmutableMap.of("type", "string"),
//                        "ingredients", ImmutableMap.of(
//                                "type", "array",
//                                "items", ImmutableMap.of("type", "string")
//                        )
//                ),
//                "required", ImmutableList.of("recipe_name", "ingredients")
//        );

        Schema responseSchema =
                Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(ImmutableMap.of(
                               "recipe_name", Schema.builder().type(Type.Known.STRING).build(),
                               "ingredients", Schema.builder()
                                        .type(Type.Known.ARRAY)
                                                .items(Schema.builder().type(Type.Known.STRING).build())
                                        .build()
                        ))
                        .required(ImmutableList.of("recipe_name", "ingredients"))
                        .build();

        // Set the response schema in GenerateContentConfig
        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .candidateCount(1)
                        .responseSchema(responseSchema)
                        .build();

        GenerateContentResponse response =
                client.models.generateContent("gemini-2.5-flash", "Tell me how to cook rice", config);

        System.out.println("Response: " + response.text());
    }
}