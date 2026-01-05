package com.darkop.nas.fs;

import com.darkop.nas.model.records.UploadSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UploadScanner {

    private final Path uploadsRoot;

    /**
     * uploadsRoot is expected to have the structure:
     * uploadsRoot/
     * └── <username>/
     * └── incoming/
     */

    public UploadScanner(Path uploadsRoot) {
        this.uploadsRoot = uploadsRoot;
    }

    public List<UploadSummary> scan() {
        List<UploadSummary> results = new ArrayList<>();

        if (!Files.isDirectory(uploadsRoot)) {
            throw new IllegalStateException("Uploads root does not exist or is not a directory: " + uploadsRoot);
        }

        try (Stream<Path> userDirs = Files.list(uploadsRoot)) {
            userDirs.filter(Files::isDirectory).forEach(userDir -> scanUser(userDir, results));
        } catch (IOException e) {
            throw new RuntimeException("Failed to list uploads root: " + uploadsRoot, e);
        }

        return results;
    }

    private void scanUser(Path userDir, List<UploadSummary> results) {
        String username = userDir.getFileName().toString();
        Path incomingDir = userDir.resolve("incoming");

        if (!Files.isDirectory(incomingDir)) {
            // User exists but has no incoming dir → zero uploads
            results.add(new UploadSummary(username, 0, 0));
            return;
        }

        long fileCount = 0, byteCount = 0;

        try (Stream<Path> paths = Files.walk(incomingDir)) {
            for (Path path : (Iterable<Path>) paths::iterator) {

                if (Files.isSymbolicLink(path)) {
                    continue;
                }

                if (Files.isRegularFile(path)) {
                    fileCount++;
                    byteCount += Files.size(path);
                }
            }
        } catch (IOException e) {
            // Per-user failure isolation
            System.err.println("[WARN] Failed to scan uploads for user '" + username + "': " + e.getMessage());

            // Still emit a row so batch logic remains deterministic
            results.add(new UploadSummary(username, 0, 0));
            return;
        }

        results.add(new UploadSummary(username, fileCount, byteCount));
    }
}