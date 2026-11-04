package com.kani.generator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync // Activates Spring's asynchronous task execution capabilities
public class AsyncConfig {

    @Bean(name = "workspaceExecutor")
    public TaskExecutor workspaceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // Max simultaneous generations allowed under normal load
        executor.setMaxPoolSize(4);        // Maximum capacity burst boundary
        executor.setQueueCapacity(50);     // Queue up requests if Ollama is running hot
        executor.setThreadNamePrefix("CodeGenWorker-");
        executor.initialize();
        return executor;
    }
}
