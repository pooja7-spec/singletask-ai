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

    // ---------------- AI PARSE TASKS ----------------
    @PostMapping("/parse")
    public ResponseEntity<?> parseTasks(@RequestBody Map<String, String> body) {
        List<Task> created = taskService.parseTasks(body.get("text"));
        return ResponseEntity.ok(Map.of("count", created.size()));
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

    // ---------------- DELETE TASK ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id) {
        boolean removed = taskService.deleteTask(id);
        return removed ? ResponseEntity.ok(Map.of("deleted", true))
                       : ResponseEntity.notFound().build();
    }

    // ---------------- CLEAR COMPLETED ----------------
    @PostMapping("/clear-completed")
    public ResponseEntity<?> clearCompleted() {
        int removed = taskService.clearCompleted();
        return ResponseEntity.ok(Map.of("removed", removed));
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

    // ---------------- AI PICK NEXT TASK ----------------
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