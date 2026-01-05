package com.darkop.nas.app;

import com.darkop.nas.config.NasConfig;
import com.darkop.nas.db.DbConfig;
import com.darkop.nas.db.PersistenceService;
import com.darkop.nas.fs.FileSorter;
import com.darkop.nas.fs.UploadScanner;
import com.darkop.nas.model.enums.BatchStatus;
import com.darkop.nas.model.records.BatchRun;
import com.darkop.nas.model.records.SortResult;
import com.darkop.nas.model.records.UploadSummary;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Path uploadsRoot = Path.of(NasConfig.uploadsRoot());
        Path mediaRoot   = Path.of(NasConfig.mediaRoot());

        LocalDateTime batchStart = LocalDateTime.now();
        LocalDateTime batchEnd;

        int totalUsersSeen = 0;
        long totalFilesSeen = 0;
        long totalBytesSeen = 0;
        long totalFilesSorted = 0;
        long totalFilesFailed = 0;

        BatchStatus status = BatchStatus.FAILED;

        List<UploadSummary> uploadSummaries = List.of();
        List<SortResult> sortResults = List.of();

        try (Connection connection = DbConfig.getConnection()) {

            try {
                System.out.println("NAS Batch Job starting...");

                // A — Scan uploads
                UploadScanner scanner = new UploadScanner(uploadsRoot);
                uploadSummaries = scanner.scan();

                totalUsersSeen = uploadSummaries.size();
                for (UploadSummary us : uploadSummaries) {
                    totalFilesSeen += us.fileCount();
                    totalBytesSeen += us.byteCount();
                }

                // B — Sort files
                FileSorter sorter = new FileSorter(uploadsRoot, mediaRoot);
                sortResults = sorter.sort();

                for (SortResult sr : sortResults) {
                    totalFilesSorted += sr.filesSorted();
                    totalFilesFailed += sr.failures().size();
                }

                status = BatchStatus.SUCCESS;
                System.out.println("NAS Batch Job completed successfully.");

            } catch (Exception e) {
                System.err.println("NAS Batch Job failed.");
                e.printStackTrace();
            } finally {

                batchEnd = LocalDateTime.now();

                BatchRun batchRun = new BatchRun(LocalDate.now(), batchStart, batchEnd, totalUsersSeen, totalFilesSeen, totalBytesSeen, totalFilesSorted, totalFilesFailed, status);

                PersistenceService persistenceService = new PersistenceService(connection);

                persistenceService.persistBatch(batchRun, uploadSummaries, sortResults);
            }

            System.exit(status == BatchStatus.SUCCESS ? 0 : 2);

        } catch (Exception fatal) {
            System.err.println("Fatal error: unable to start batch job");
            fatal.printStackTrace();
            System.exit(3);
        }
    }
}
