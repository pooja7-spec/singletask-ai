package com.pooja.singletask;

import com.pooja.singletask.ai.EmotionService;
import com.pooja.singletask.ai.GroqService;
import com.pooja.singletask.ai.TaskScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final List<Task> tasks = new CopyOnWriteArrayList<>();

    private final GroqService groqService;
    private final EmotionService emotionService;
    private final TaskScoringService scoringService;

    // ---------------- ADD TASK ----------------
    public Task addTask(String rawText, String title) {
        Task task = Task.builder()
                .id(UUID.randomUUID().toString())
                .rawText(rawText)
                .title(title)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();
        tasks.add(task);
        return task;
    }

    // ---------------- NLP PARSING ----------------
    public List<Map<String, Object>> parseTasks(String text) {
        return groqService.parseTasks(text);
    }

    // ---------------- EMOTION ----------------
    public void setEmotion(String state) {
        emotionService.setEmotion(state);
    }

    // ---------------- GET ALL TASKS ----------------
    public List<Task> getAllTasks() {
        return tasks;
    }

    // ---------------- GET CURRENT TASK (old behavior) ----------------
    public Optional<Task> getCurrentTask() {
        return tasks.stream()
                .filter(t -> "PENDING".equals(t.getStatus()))
                .sorted(Comparator.comparing(Task::getCreatedAt))
                .findFirst();
    }

    // ---------------- GET NEXT TASK (AI scoring) ----------------
    public Optional<Task> getNextTask() {
        return tasks.stream()
                .filter(t -> "PENDING".equals(t.getStatus()))
                .sorted((a, b) -> scoringService.score(b) - scoringService.score(a))
                .findFirst();
    }

    // ---------------- COMPLETE TASK ----------------
    public Optional<Task> completeTask(String id) {
        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .map(t -> {
                    t.setStatus("DONE");
                    return t;
                });
    }
}