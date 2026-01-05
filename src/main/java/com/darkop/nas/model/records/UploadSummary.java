package com.darkop.nas.model.records;

public record UploadSummary(
        String username,
        long fileCount,
        long byteCount
) {}