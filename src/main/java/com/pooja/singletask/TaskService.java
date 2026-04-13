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

    // ---- SAFE STRING ----
    private String safe(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    // ---- CATEGORY NORMALIZATION ----
    private String normalizePriority(String p) {
        if (p == null) return "medium";
        p = p.toLowerCase();

        if (p.contains("high") || p.equals("3") || p.equals("4") || p.equals("5")) return "high";
        if (p.contains("low") || p.equals("1")) return "low";
        return "medium";
    }

    private String normalizeDifficulty(String d) {
        if (d == null) return "medium";
        d = d.toLowerCase();

        if (d.contains("hard") || d.equals("4") || d.equals("5")) return "hard";
        if (d.contains("easy") || d.equals("1")) return "easy";
        return "medium";
    }

    private String normalizeEnergy(String e) {
        if (e == null) return "medium";
        e = e.toLowerCase();

        if (e.contains("high") || e.equals("4") || e.equals("5")) return "high";
        if (e.contains("low") || e.equals("1") || e.equals("2")) return "low";
        return "medium";
    }

    private String normalizeDuration(String d) {
        if (d == null) return "medium";
        d = d.toLowerCase();

        try {
            double hours = Double.parseDouble(d);
            if (hours <= 1) return "short";
            if (hours <= 3) return "medium";
            return "long";
        } catch (Exception ignored) {}

        if (d.contains("short")) return "short";
        if (d.contains("long")) return "long";
        return "medium";
    }

    private String normalizeDeadline(String d) {
        if (d == null) return "none";
        d = d.toLowerCase();

        if (d.contains("today") || d.contains("asap")) return "today";
        if (d.contains("tomorrow") || d.contains("soon") || d.contains("end of month")) return "tomorrow";
        return "none";
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

            String title = safe(map.get("title"));

            // ---- PREVENT DUPLICATE PENDING TASKS ----
            boolean exists = tasks.stream()
                    .anyMatch(t -> t.getTitle().equalsIgnoreCase(title)
                            && "PENDING".equals(t.getStatus()));

            if (exists) {
                continue; // skip duplicate pending tasks
            }

            Task task = Task.builder()
                    .id(UUID.randomUUID().toString())
                    .rawText(title)
                    .title(title)
                    .priority(normalizePriority(safe(map.get("priority"))))
                    .difficulty(normalizeDifficulty(safe(map.get("difficulty"))))
                    .energy(normalizeEnergy(safe(map.get("energy"))))
                    .duration(normalizeDuration(safe(map.get("duration"))))
                    .deadline(normalizeDeadline(safe(map.get("deadline"))))
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