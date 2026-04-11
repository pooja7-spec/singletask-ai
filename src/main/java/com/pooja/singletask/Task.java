package com.pooja.singletask;

import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    private String id;

    private String rawText;
    private String title;

    private String status; // PENDING / DONE
    private Instant createdAt;

    // ---- AI Metadata ----
    private String priority;     // high / medium / low
    private String difficulty;   // easy / medium / hard
    private String energy;       // low / medium / high
    private String duration;     // short / medium / long
    private String deadline;     // today / tomorrow / date string
}