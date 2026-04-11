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

        // ---- PRIORITY ----
        if ("high".equalsIgnoreCase(t.getPriority())) s += 5;
        if ("medium".equalsIgnoreCase(t.getPriority())) s += 3;
        if ("low".equalsIgnoreCase(t.getPriority())) s += 1;

        // ---- DIFFICULTY ----
        if ("easy".equalsIgnoreCase(t.getDifficulty())) s += 3;
        if ("medium".equalsIgnoreCase(t.getDifficulty())) s += 1;
        if ("hard".equalsIgnoreCase(t.getDifficulty())) s -= 2;

        // ---- ENERGY ----
        if ("low".equalsIgnoreCase(t.getEnergy())) s += 2;
        if ("medium".equalsIgnoreCase(t.getEnergy())) s += 1;
        if ("high".equalsIgnoreCase(t.getEnergy())) s -= 1;

        // ---- DURATION ----
        if ("short".equalsIgnoreCase(t.getDuration())) s += 3;
        if ("medium".equalsIgnoreCase(t.getDuration())) s += 1;
        if ("long".equalsIgnoreCase(t.getDuration())) s -= 1;

        // ---- DEADLINE ----
        if ("today".equalsIgnoreCase(t.getDeadline())) s += 4;
        if ("tomorrow".equalsIgnoreCase(t.getDeadline())) s += 2;

        // ---- EMOTION MULTIPLIERS ----
        String emotion = emotionService.getEmotion();

        if ("tired".equalsIgnoreCase(emotion)) {
            if ("low".equalsIgnoreCase(t.getEnergy())) s += 5;
            if ("short".equalsIgnoreCase(t.getDuration())) s += 3;
        }

        if ("stressed".equalsIgnoreCase(emotion)) {
            if ("short".equalsIgnoreCase(t.getDuration())) s += 5;
            if ("easy".equalsIgnoreCase(t.getDifficulty())) s += 3;
        }

        if ("overwhelmed".equalsIgnoreCase(emotion)) {
            if ("short".equalsIgnoreCase(t.getDuration())) s += 6;
            if ("low".equalsIgnoreCase(t.getEnergy())) s += 4;
        }

        if ("motivated".equalsIgnoreCase(emotion)) {
            if ("high".equalsIgnoreCase(t.getPriority())) s += 6;
            if ("hard".equalsIgnoreCase(t.getDifficulty())) s += 4;
        }

        return s;
    }
}