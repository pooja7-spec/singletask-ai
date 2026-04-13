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

    // ---- SAFE STRING CONVERSION ----
    private String safe(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    // ---------------- ADD TASK (manual) ----------------
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

    // ---------------- AI PARSE TASKS ----------------
    public List<Task> parseTasks(String text) {
        List<Map<String, Object>> parsed = groqService.parseTasks(text);
        List<Task> created = new ArrayList<>();

        for (Map<String, Object> map : parsed) {
            Task task = Task.builder()
                    .id(UUID.randomUUID().toString())
                    .rawText(safe(map.get("title")))
                    .title(safe(map.get("title")))
                    .priority(safe(map.get("priority")))
                    .difficulty(safe(map.get("difficulty")))
                    .energy(safe(map.get("energy")))
                    .duration(safe(map.get("duration")))
                    .deadline(safe(map.get("deadline")))
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .build();

            tasks.add(task);
            created.add(task);
        }

        return created;
    }

    // ---------------- EMOTION ----------------
    public void setEmotion(String state) {
        emotionService.setEmotion(state);
    }

    // ---------------- GET ALL TASKS ----------------
    public List<Task> getAllTasks() {
        return tasks;
    }

    // ---------------- GET CURRENT TASK ----------------
    public Optional<Task> getCurrentTask() {
        return tasks.stream()
                .filter(t -> "PENDING".equals(t.getStatus()))
                .sorted(Comparator.comparing(Task::getCreatedAt))
                .findFirst();
    }

    // ---------------- AI PICK NEXT TASK ----------------
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