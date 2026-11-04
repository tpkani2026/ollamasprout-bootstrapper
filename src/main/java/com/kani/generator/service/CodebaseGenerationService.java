package com.kani.generator.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kani.generator.util.CodeSanitizer;

import reactor.core.publisher.Flux;

@Service
public class CodebaseGenerationService {

    private final ChatClient geminiChatClient;

    /**
     * Constructor injection wrapping the official Spring AI Google Gen AI model instance.
     */
    public CodebaseGenerationService(GoogleGenAiChatModel googleGenAiChatModel) {
        this.geminiChatClient = ChatClient.builder(googleGenAiChatModel).build();
    }

    private void updateJobLog(String jobId, String message) {
        List<String> logs = com.kani.generator.controller.ToolController.jobLogs.computeIfAbsent(jobId, k -> new java.util.concurrent.CopyOnWriteArrayList<>());
        logs.add(message);
    }

    private void saveFileToDisk(String baseDir, String relativePath, String content) throws IOException {
        Path fullPath = Paths.get(baseDir, relativePath);
        Files.createDirectories(fullPath.getParent());
        
        // Clean, sanitize, and remove all syntax noise prior to hitting the drive layer
        String cleanContent = CodeSanitizer.sanitize(relativePath, content);
        Files.writeString(fullPath, cleanContent);
    }

    /**
     * Asynchronously generates application layers and manifests files cleanly to disk.
     * Uses reactive line-by-line streaming sockets to process Gemini 3.8 Flash outputs.
     */
    @Async("workspaceExecutor")
    public void generateWorkspace(String functionalRequirementScript, String targetOutputDirectory, String jobId) {
        String systemPrompt = """
            You are an Expert Enterprise Java 21 Architect and Automated Workspace Generator.
            Provide complete, working Java source files or application configurations based on expanding the user's template guidelines.
            
            Strict Structural Boundaries:
            1. Every separate component, class, enum, configuration, or interface MUST be generated as an independent block.
            2. Before starting ANY file, you MUST explicitly print a file path header tag on its own line exactly like this:
               [FILE: src/main/java/your/package/Name.java] or [FILE: pom.xml]
            3. Do not combine multiple components or nest enums/classes inside other classes.
            4. Wrap the complete code implementation directly below the tag using standard triple backticks.
            5. Never output placeholders, code shortcuts, or comments like '// TODO'. Replace all comment instructions with completely functional production code.
            6. CRITICAL ASSIGNMENT: You MUST automatically generate and append the main Spring Boot bootstrapper entrypoint class under the target path:
               [FILE: src/main/java/com/kani/SimpleCrudOperationApplication.java]
            """;

        long startTimeMillis = System.currentTimeMillis();
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");

        System.out.println("[SOP LOG] [JOB-" + jobId + "] Launching cloud code generation pipeline targeting path: " + targetOutputDirectory);
        updateJobLog(jobId, "⏳ Job Initialization Started at: " + startDateTime.format(timeFormatter));
        updateJobLog(jobId, "🤖 Contacting Google Gemini APIs to initialize live streaming layout generation...");
        
        try {
            Flux<String> responseStream = geminiChatClient.prompt()
                    .system(systemPrompt)
                    .user(functionalRequirementScript)
                    .stream()
                    .content();

            // Stateful accumulators for inline reactive token collection
            final StringBuilder lineBuffer = new StringBuilder();
            final java.util.concurrent.atomic.AtomicReference<String> currentPath = new java.util.concurrent.atomic.AtomicReference<>(null);
            final java.util.concurrent.atomic.AtomicReference<StringBuilder> contentBuffer = new java.util.concurrent.atomic.AtomicReference<>(new StringBuilder());
            final java.util.concurrent.atomic.AtomicInteger filesCounter = new java.util.concurrent.atomic.AtomicInteger(0);

            responseStream.doOnNext(token -> {
                System.out.print(".");
                System.out.flush();
                
                lineBuffer.append(token);
                String bufferStr = lineBuffer.toString();
                
                if (bufferStr.contains("\n")) {
                    String[] lines = bufferStr.split("\n", -1);
                    lineBuffer.setLength(0);
                    lineBuffer.append(lines[lines.length - 1]);
                    
                    for (int i = 0; i < lines.length - 1; i++) {
                        String currentLine = lines[i].trim();
                        
                        // Parse file boundaries dynamically case-insensitively
                        if (currentLine.toUpperCase().startsWith("[FILE:") && currentLine.endsWith("]")) {
                            // Flush active file buffer to drive immediately if populated
                            if (currentPath.get() != null && contentBuffer.get().length() > 0) {
                                try {
                                    saveFileToDisk(targetOutputDirectory, currentPath.get(), contentBuffer.get().toString());
                                    System.out.println("\n[SOP LOG] [LIVE COMPILER] Saved file: " + currentPath.get());
                                    updateJobLog(jobId, "📁 Physically Manifested: " + currentPath.get());
                                    filesCounter.incrementAndGet();
                                } catch (IOException e) {
                                    System.err.println("\n[SOP ERROR] Drive write block failed for: " + currentPath.get());
                                }
                            }
                            
                            // Initialize parameters for the next incoming file node
                            String pathToken = currentLine.substring(currentLine.indexOf(":") + 1, currentLine.length() - 1).trim();
                            currentPath.set(pathToken);
                            contentBuffer.set(new StringBuilder());
                        } else {
                            // Collect source data lines if a valid target path scope is open
                            if (currentPath.get() != null) {
                                contentBuffer.get().append(lines[i]).append("\n");
                            }
                        }
                    }
                }
            }).doOnError(err -> {
                System.err.println("\n[SOP CRITICAL ERROR] [JOB-" + jobId + "] Streaming interrupted: " + err.getMessage());
                com.kani.generator.controller.ToolController.jobStatuses.put(jobId, "FAILED");
                updateJobLog(jobId, "❌ Cloud Streaming Pipeline Interrupted: " + err.getMessage());
            }).doOnComplete(() -> {
                // Safely commit the tail file payload remaining inside the stream loop buffer
                if (currentPath.get() != null && contentBuffer.get().length() > 0) {
                    try {
                        saveFileToDisk(targetOutputDirectory, currentPath.get(), contentBuffer.get().toString());
                        System.out.println("\n[SOP LOG] [LIVE COMPILER] Saved file: " + currentPath.get());
                        updateJobLog(jobId, "📁 Physically Manifested: " + currentPath.get());
                        filesCounter.incrementAndGet();
                    } catch (IOException e) {
                        System.err.println("\n[SOP ERROR] Tail context commit failed: " + e.getMessage());
                    }
                }
                
                long durationSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
                updateJobLog(jobId, "✅ Workspace generation finalized successfully! Generated " + filesCounter.get() + " files in " + durationSeconds + "s.");
                com.kani.generator.controller.ToolController.jobStatuses.put(jobId, "COMPLETED");
                System.out.println("\n[SOP LOG] [JOB-" + jobId + "] Execution loop finalized successfully.");
            }).subscribe();

        } catch (Exception e) {
            System.err.println("[SOP ERROR] Failed to mount stream context variables: " + e.getMessage());
            updateJobLog(jobId, "❌ Initialization Failure: " + e.getMessage());
            com.kani.generator.controller.ToolController.jobStatuses.put(jobId, "FAILED");
        }
    }
}
