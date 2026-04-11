package com.pooja.singletask.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GroqService {

    private final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final String API_KEY = System.getenv("GROQ_API_KEY");

    public List<Map<String, Object>> parseTasks(String text) {
        try {
            RestTemplate rest = new RestTemplate();

            Map<String, Object> request = new HashMap<>();
            request.put("model", "llama3-8b-8192");
            request.put("temperature", 0.2);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", "Extract tasks from text. Return ONLY a JSON array. Each task must include: title, priority, difficulty, energy, duration, deadline."
            ));
            messages.add(Map.of("role", "user", "content", text));

            request.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            Map response = rest.postForObject(API_URL, entity, Map.class);

            // ---- FIXED JSON CASTING ----
            Map<String, Object> choice = (Map<String, Object>) ((List<?>) response.get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            String content = (String) message.get("content");

            return new ObjectMapper().readValue(content, List.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse tasks via Groq: " + e.getMessage());
        }
    }
}