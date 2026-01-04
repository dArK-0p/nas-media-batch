package com.darkop.nas.app;

import com.darkop.nas.fs.UploadScanner;
import com.darkop.nas.model.UploadSummary;

import java.nio.file.Path;
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

        try {
            UploadScanner scanner = new UploadScanner(uploadsRoot);
            List<UploadSummary> summaries = scanner.scan();

            summaries.forEach(System.out::println);

            LocalDateTime batchEnd = LocalDateTime.now();
            System.out.println("NAS Batch Job completed successfully.");
            System.out.println("Batch duration: " + java.time.Duration.between(batchStart, batchEnd));

        } catch (Exception e) {
            System.err.println("NAS Batch Job failed.");
            e.printStackTrace();
            System.exit(2);
        }
    }
}
