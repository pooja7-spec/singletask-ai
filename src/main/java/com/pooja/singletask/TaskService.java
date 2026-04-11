package com.pooja.singletask;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TaskService {

    private final List<Task> tasks = new CopyOnWriteArrayList<>();

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

    // ---------------- GET NEXT TASK ----------------
    public Optional<Task> getNextTask() {
        return getCurrentTask();
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