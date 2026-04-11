package com.pooja.singletask.ai;

import com.pooja.singletask.Task;
import org.springframework.stereotype.Service;

@Service
public class TaskScoringService {

    private final EmotionService emotionService;

    public TaskScoringService(EmotionService emotionService) {
        this.emotionService = emotionService;
    }

    public int score(Task t) {
        int s = 0;

        // Priority
        if ("high".equalsIgnoreCase(t.getPriority())) s += 3;
        if ("medium".equalsIgnoreCase(t.getPriority())) s += 2;

        // Difficulty
        if ("easy".equalsIgnoreCase(t.getDifficulty())) s += 2;

        // Emotional matching
        String emotion = emotionService.getEmotion();

        if ("tired".equals(emotion) && "low".equalsIgnoreCase(t.getEnergy())) s += 3;
        if ("stressed".equals(emotion) && "short".equalsIgnoreCase(t.getDuration())) s += 2;
        if ("motivated".equals(emotion) && "high".equalsIgnoreCase(t.getEnergy())) s += 3;

        return s;
    }
}