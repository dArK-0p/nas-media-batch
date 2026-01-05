package com.darkop.nas.fs;

import com.darkop.nas.model.enums.FileFailureAction;
import com.darkop.nas.model.enums.FileFailureStage;
import com.darkop.nas.model.enums.FileFailureType;
import com.darkop.nas.model.records.FileFailure;
import com.darkop.nas.model.records.SortResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileSorter {

    private final Path uploadsRoot;
    private final Path mediaRoot;

    public FileSorter(Path uploadsRoot, Path mediaRoot) {
        this.uploadsRoot = uploadsRoot;
        this.mediaRoot = mediaRoot;
    }

    public List<SortResult> sort() {
        validateRoots();

        List<SortResult> results = new ArrayList<>();

        try (Stream<Path> users = Files.list(uploadsRoot)) {
            users.filter(Files::isDirectory).forEach(userDir -> results.add(sortUser(userDir)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to list uploads root: " + uploadsRoot, e);
        }

        return results;
    }

    private SortResult sortUser(Path userDir) {
        String username = userDir.getFileName().toString();
        Path incoming = userDir.resolve("incoming");
        Path quarantine = userDir.resolve("quarantine");

        long filesSorted = 0;
        long bytesSorted = 0;

        List<FileFailure> failures = new ArrayList<>();

        if (!Files.exists(incoming) || !Files.isDirectory(incoming)) {
            return new SortResult(username, 0, 0, failures);
        }

        try {
            Files.createDirectories(quarantine);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create quarantine for user " + username, e);
        }

        try (Stream<Path> paths = Files.walk(incoming)) {
            List<Path> sources = paths.filter(Files::isRegularFile).filter(p -> !Files.isSymbolicLink(p)).toList();

            for (Path source : sources) {
                try {
                    long size = Files.size(source);

                    YearMonth ym = YearMonth.from(Files.getLastModifiedTime(source).toInstant().atZone(java.time.ZoneId.systemDefault()));

                    Path targetDir = mediaRoot.resolve(String.valueOf(ym.getYear())).resolve(String.format("%02d", ym.getMonthValue()));

                    Files.createDirectories(targetDir);

                    Path target = targetDir.resolve(source.getFileName());

                    Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);

                    filesSorted++;
                    bytesSorted += size;

                } catch (Exception ex) {
                    failures.add(new FileFailure(username, source.toAbsolutePath().toString(), safeSize(source), FileFailureType.MOVE_FAILED, FileFailureStage.MOVE, FileFailureAction.QUARANTINED, LocalDateTime.now()));

                    quarantineFile(source, quarantine);
                }
            }
        } catch (IOException e) {
            failures.add(new FileFailure(username, incoming.toAbsolutePath().toString(), -1, FileFailureType.DIRECTORY_SCAN_FAILED, FileFailureStage.SCAN, FileFailureAction.SKIPPED, LocalDateTime.now()));
        }

        return new SortResult(username, filesSorted, bytesSorted, failures);
    }

    private void quarantineFile(Path source, Path quarantineDir) {
        try {
            Files.move(source, quarantineDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    private long safeSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return -1;
        }
    }

    private void validateRoots() {
        if (!Files.exists(uploadsRoot) || !Files.isReadable(uploadsRoot)) {
            throw new IllegalStateException("Uploads root invalid: " + uploadsRoot);
        }

        if (!Files.exists(mediaRoot) || !Files.isWritable(mediaRoot)) {
            throw new IllegalStateException("Media root invalid: " + mediaRoot);
        }
    }
}
