package com.darkop.nas.model.records;

import com.darkop.nas.model.enums.FileFailureAction;
import com.darkop.nas.model.enums.FileFailureStage;
import com.darkop.nas.model.enums.FileFailureType;

import java.time.LocalDateTime;

public record FileFailure(
        String username,
        String path,
        long size,
        FileFailureType type,
        FileFailureStage stage,
        FileFailureAction actionTaken,
        LocalDateTime timestamp
) {}