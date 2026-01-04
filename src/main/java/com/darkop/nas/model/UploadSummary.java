package com.darkop.nas.model;

public record UploadSummary(
        String username,
        long fileCount,
        long byteCount
) {}