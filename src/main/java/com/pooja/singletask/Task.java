package com.pooja.singletask;

import lombok.*;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    private String id;
    private String rawText;
    private String title;
    private String status; // PENDING, DONE
    private Instant createdAt;
}