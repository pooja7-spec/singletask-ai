package com.pooja.singletask;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public Task createTask(@RequestBody Map<String, String> body) {
        String rawText = body.get("text");
        Task task = taskService.addTask(rawText, rawText); // AI later
        broadcastCurrentTask();
        return task;
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable String id) {
        return taskService.completeTask(id)
                .map(task -> {
                    broadcastCurrentTask();
                    return ResponseEntity.ok(task);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/current")
    public ResponseEntity<Task> getCurrentTask() {
        return taskService.getCurrentTask()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    private void broadcastCurrentTask() {
    Object payload = taskService.getCurrentTask().orElse(null);
    messagingTemplate.convertAndSend("/topic/current-task", payload);
    }
}