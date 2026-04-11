package com.pooja.singletask.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final GroqService groqService;
    private final EmotionService emotionService;

    // ---- NLP PARSE ----
    @PostMapping("/parse")
    public ResponseEntity<?> parse(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        return ResponseEntity.ok(groqService.parseTasks(text));
    }

    // ---- EMOTION ----
    @PostMapping("/emotion")
    public ResponseEntity<?> setEmotion(@RequestBody Map<String, String> body) {
        String state = body.get("state");
        emotionService.setEmotion(state);
        return ResponseEntity.ok(Map.of("status", "updated"));
    }
}