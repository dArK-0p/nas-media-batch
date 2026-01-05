package com.darkop.nas.model.records;

import java.util.List;

public record SortResult(
        String username,
        long filesSorted,
        long filesFailed,
        List<FileFailure> failureLog
) {}
