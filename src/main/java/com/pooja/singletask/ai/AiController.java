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

    @PostMapping("/nlp")
    public ResponseEntity<?> parse(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(groqService.parseTasks(body.get("text")));
    }

    @PostMapping("/emotion")
    public ResponseEntity<?> setEmotion(@RequestBody Map<String, String> body) {
        emotionService.setEmotion(body.get("state"));
        return ResponseEntity.ok(Map.of("status", "updated"));
    }
}