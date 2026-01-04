package com.darkop.nas.app;

import com.darkop.nas.db.PersistenceService;
import com.darkop.nas.fs.UploadScanner;
import com.darkop.nas.model.BatchRun;
import com.darkop.nas.model.BatchStatus;
import com.darkop.nas.model.UploadSummary;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("NAS Batch Job starting...");

        if (args.length != 1) {
            System.err.println("Usage: java -jar nas-media-batch.jar <uploads-root>");
            System.exit(1);
        }

        Path uploadsRoot = Path.of(args[0]);

        LocalDateTime batchStart = LocalDateTime.now();
        LocalDateTime batchEnd;

        int totalUsersSeen = 0;
        long totalFilesSeen = 0;
        long totalBytesSeen = 0;

        BatchStatus status = BatchStatus.FAILED;

        List<UploadSummary> summaries = List.of();
        Exception failure = null;

        try {
            UploadScanner scanner = new UploadScanner(uploadsRoot);
            summaries = scanner.scan();

            totalUsersSeen = summaries.size();

            for (UploadSummary summary : summaries) {
                totalFilesSeen += summary.fileCount();
                totalBytesSeen += summary.byteCount();
            }

            status = BatchStatus.SUCCESS;
            System.out.println("NAS Batch Job completed successfully.");

        } catch (Exception e) {
            failure = e;
            System.err.println("NAS Batch Job failed.");

        } finally {
            batchEnd = LocalDateTime.now();

            BatchRun batchRun = new BatchRun(
                    LocalDate.now(),
                    batchStart,
                    batchEnd,
                    totalUsersSeen,
                    totalFilesSeen,
                    totalBytesSeen,
                    status
            );

            try {
                PersistenceService persistenceService = new PersistenceService();
                persistenceService.persist(batchRun, summaries);
            } catch (Exception e) {
                System.err.println("Failed to persist batch results.");
                e.printStackTrace();
            }
        }

        if (status == BatchStatus.FAILED) {
            if (failure != null) {
                failure.printStackTrace();
            }
            System.exit(2);
        }
    }
}
