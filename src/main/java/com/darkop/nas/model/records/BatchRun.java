package com.darkop.nas.model.records;

import com.darkop.nas.model.enums.BatchStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BatchRun(LocalDate runDate,

                       LocalDateTime startTime, LocalDateTime endTime,

                       int totalUsersSeen, long totalFilesSeen, long totalBytesSeen,

                       long totalFilesSorted, long totalFilesFailed,

                       BatchStatus status) {
}
