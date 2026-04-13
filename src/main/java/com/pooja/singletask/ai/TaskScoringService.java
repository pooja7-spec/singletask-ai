package com.pooja.singletask.ai;

import com.pooja.singletask.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskScoringService {

    private final EmotionService emotionService;

    public int score(Task t) {
        int score = 0;

        // ---------------- BASE SCORE ----------------
        score += switch (t.getPriority()) {
            case "high" -> 30;
            case "medium" -> 20;
            default -> 10;
        };

        score += switch (t.getDifficulty()) {
            case "hard" -> 30;
            case "medium" -> 20;
            default -> 10;
        };

        score += switch (t.getEnergy()) {
            case "high" -> 30;
            case "medium" -> 20;
            default -> 10;
        };

        score += switch (t.getDuration()) {
            case "long" -> 30;
            case "medium" -> 20;
            default -> 10;
        };

        score += switch (t.getDeadline()) {
            case "today" -> 40;
            case "tomorrow" -> 20;
            default -> 0;
        };

        // ---------------- EMOTION BOOST ----------------
        String emotion = emotionService.getEmotion();

        if ("tired".equals(emotion)) {
            if ("low".equals(t.getEnergy())) score += 50;
            if ("short".equals(t.getDuration())) score += 40;
            if ("easy".equals(t.getDifficulty())) score += 30;
            if ("low".equals(t.getPriority()) || "medium".equals(t.getPriority())) score += 10;
        }

        else if ("overwhelmed".equals(emotion)) {
            if ("short".equals(t.getDuration())) score += 60;
            if ("easy".equals(t.getDifficulty())) score += 40;
            if ("low".equals(t.getEnergy()) || "medium".equals(t.getEnergy())) score += 20;
            if ("medium".equals(t.getPriority())) score += 10;
        }

        else if ("motivated".equals(emotion)) {
            if ("high".equals(t.getPriority())) score += 60;
            if ("hard".equals(t.getDifficulty())) score += 40;
            if ("high".equals(t.getEnergy())) score += 30;
            if ("long".equals(t.getDuration()) || "medium".equals(t.getDuration())) score += 20;
        }

        return score;
    }
}