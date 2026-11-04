package com.kani.generator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeSanitizer {

    private static final Pattern MARKDOWN_CODE_BLOCK_PATTERN = 
            Pattern.compile("```(?:java|xml|properties)?\\s*(.*?)\\s*```", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Cleans code structural blocks, strips raw markdown tokens, and forces 
     * correct package line alignments to ensure error-free compiling inside Eclipse.
     */
    public static String sanitize(String filePath, String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return "";
        }

        String processed = rawCode.trim();

        // 1. Clean up potential markdown wrappers
        Matcher matcher = MARKDOWN_CODE_BLOCK_PATTERN.matcher(processed);
        if (matcher.find()) {
            processed = matcher.group(1);
        }

        // Clean out any lingering raw backticks at the beginning or end of text segments
        processed = processed.replace("```", "").trim();

        String pathLower = filePath.toLowerCase();

        // 2. Fix Java Persistence layer namespace migrations and force strict Line 1 package alignment
        if (pathLower.endsWith(".java")) {
            processed = processed.replace("javax.persistence", "jakarta.persistence");
            
            // CRITICAL FIX: Strip all invisible ghost spaces, markdown text, or comments placed before the package keyword
            if (processed.contains("package ")) {
                processed = processed.substring(processed.indexOf("package "));
            }
        }

        return processed.trim();
    }
}
