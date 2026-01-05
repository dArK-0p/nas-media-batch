package com.darkop.nas.model.records;

import com.darkop.nas.model.enums.BatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BatchRun(
        LocalDate runDate,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int totalUsers,
        long totalFilesSeen,
        long totalBytesSeen,
        BatchStatus status
) {}