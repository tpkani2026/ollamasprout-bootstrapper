package com.kani.generator.controller;

import com.kani.generator.service.CodebaseGenerationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/tool")
@CrossOrigin(origins = "*")
public class ToolController {

    private final CodebaseGenerationService generationService;
    
    // Globally accessible thread-safe structures for monitoring worker progress logs
    public static final Map<String, List<String>> jobLogs = new ConcurrentHashMap<>();
    public static final Map<String, String> jobStatuses = new ConcurrentHashMap<>();

    public ToolController(CodebaseGenerationService generationService) {
        this.generationService = generationService;
    }

    /**
     * Main action endpoint invoked by the dashboard button. Fires generation asynchronously.
     */
    @PostMapping("/compile-workspace")
    public ResponseEntity<Map<String, String>> triggerGeneration(@RequestBody Map<String, String> payload) {
        String script = payload.get("script");
        String workspacePath = payload.get("workspacePath");

        if (script == null || script.isBlank() || workspacePath == null || workspacePath.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "Error", 
                "message", "Missing required payload values: 'script' or 'workspacePath'."
            ));
        }

        // Provision a clean, unique generation request trace token
        String jobId = UUID.randomUUID().toString();
        
        // Initialize the logging state array maps
        jobStatuses.put(jobId, "RUNNING");
        List<String> logs = Collections.synchronizedList(new ArrayList<>());
        logs.add("⏳ Execution request accepted. Preparing worker threads...");
        jobLogs.put(jobId, logs);

        // Forward parameters and job identifier directly to the async service execution layer
        generationService.generateWorkspace(script, workspacePath, jobId);

        // Instantly hand control back to the browser interface to prevent server timing loops
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
            "status", "Accepted",
            "jobId", jobId,
            "message", "Workspace compilation pipeline initiated."
        ));
    }

    /**
     * Rapid status extraction route hit sequentially by the UI interval polling runtime.
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        if (!jobStatuses.containsKey(jobId)) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> statusReport = new HashMap<>();
        statusReport.put("status", jobStatuses.get(jobId));
        statusReport.put("logs", new ArrayList<>(jobLogs.getOrDefault(jobId, List.of())));
        
        return ResponseEntity.ok(statusReport);
    }

    /**
     * Evicts completed job logs from the heap map structures to prevent memory leaks over time.
     */
    @DeleteMapping("/status/clear/{jobId}")
    public ResponseEntity<Map<String, String>> purgeJobContext(@PathVariable String jobId) {
        jobLogs.remove(jobId);
        jobStatuses.remove(jobId);
        return ResponseEntity.ok(Map.of(
            "status", "Evicted", 
            "message", "Memory structure cleared successfully from the background maps."
        ));
    }
}
