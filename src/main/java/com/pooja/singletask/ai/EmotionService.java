package com.pooja.singletask.ai;

import org.springframework.stereotype.Service;

@Service
public class EmotionService {

    private String state = "neutral";

    public void setEmotion(String emotion) {
        this.state = emotion.toLowerCase();
    }

    public String getEmotion() {
        return state;
    }
}