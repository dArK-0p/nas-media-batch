package com.darkop.nas.app;

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
        LocalDateTime batchStart = LocalDateTime.now(), batchEnd;

        int totalUsersSeen = 0;
        
        long totalFilesSeen = 0, totalBytesSeen = 0, totalFilesMoved = 0, totalFilesFailed = 0;
        
        BatchStatus status = BatchStatus.FAILED;
        
        BatchRun batchRun;

        try {
            UploadScanner scanner = new UploadScanner(uploadsRoot);
            List<UploadSummary> summaries = scanner.scan();

            summaries.forEach(System.out::println); //temp

            totalUsersSeen = summaries.size();

            for(UploadSummary summary : summaries) {
                totalFilesSeen += summary.fileCount();
                totalBytesSeen += summary.byteCount();
            }

            //TODO: Add file-move stage and failure accounting.

            batchEnd = LocalDateTime.now();
            System.out.println("NAS Batch Job completed successfully.");
            status = BatchStatus.SUCCESS;

            batchRun = new BatchRun(
                    LocalDate.now(),
                    batchStart, batchEnd,
                    totalUsersSeen,
                    totalFilesSeen, totalBytesSeen,
                    totalFilesMoved, totalFilesFailed,
                    status
            );
            
            System.out.println(batchRun);

        } catch (Exception e) {
            batchEnd = LocalDateTime.now();
            System.err.println("NAS Batch Job failed.");

            batchRun = new BatchRun(
                    LocalDate.now(),
                    batchStart, batchEnd,
                    totalUsersSeen,
                    totalFilesSeen, totalBytesSeen,
                    totalFilesMoved, totalFilesFailed,
                    status
            );            
            
            e.printStackTrace();
            System.out.println(batchRun);
            
            System.exit(2);
        }

        //TODO: To implement persistence to db.
    }
}