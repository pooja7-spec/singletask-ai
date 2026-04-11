package com.pooja.singletask;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final SimpMessagingTemplate messagingTemplate;

    // ---------------- CREATE TASK ----------------
    @PostMapping
    public Task createTask(@RequestBody Map<String, String> body) {
        String rawText = body.get("text");
        Task task = taskService.addTask(rawText, rawText);
        broadcastCurrentTask();
        return task;
    }

    // ---------------- NLP PARSING ----------------
    @PostMapping("/nlp")
    public ResponseEntity<?> parseTasks(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(taskService.parseTasks(body.get("text")));
    }

    // ---------------- EMOTION ----------------
    @PostMapping("/emotion")
    public ResponseEntity<?> setEmotion(@RequestBody Map<String, String> body) {
        taskService.setEmotion(body.get("state"));
        return ResponseEntity.ok(Map.of("status", "updated"));
    }

    // ---------------- COMPLETE TASK ----------------
    @PostMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable String id) {
        return taskService.completeTask(id)
                .map(task -> {
                    broadcastCurrentTask();
                    return ResponseEntity.ok(task);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------- GET CURRENT TASK ----------------
    @GetMapping("/current")
    public ResponseEntity<Task> getCurrentTask() {
        return taskService.getCurrentTask()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ---------------- GET ALL TASKS ----------------
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    // ---------------- GET NEXT TASK (AI scoring) ----------------
    @GetMapping("/next")
    public ResponseEntity<Task> getNextTask() {
        return taskService.getNextTask()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ---------------- BROADCAST ----------------
    private void broadcastCurrentTask() {
        Object payload = taskService.getCurrentTask().orElse(null);
        messagingTemplate.convertAndSend("/topic/current-task", payload);
    }
}