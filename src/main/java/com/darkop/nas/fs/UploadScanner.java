package com.darkop.nas.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UploadScanner {
    Path uploadsRoot;

    public UploadScanner(Path uploadsRoot) {
        this.uploadsRoot = uploadsRoot;
    }

    public List<UploadSummary> getUploadSummary() {
        List<String> userList = getUsers();
        List<UploadSummary> result = new ArrayList<>();

        for(String user : userList) {
            long[] fileByte = getCountAndBytes(user);

            long fileCount = fileByte[0];
            long byteCount = fileByte[1];

            UploadSummary us = new UploadSummary(user, fileCount, byteCount);
            result.add(us);
        }

        return result;
    }

    private List<String> getUsers() {
        List<String> users = null;

        try (Stream<Path> listRoot = Files.list(uploadsRoot)) {
            users = listRoot
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
        } catch (IOException E) {
            System.out.println("Error!");
        }

        return users;
    }

    private long[] getCountAndBytes(String user) {
        Path incomingDir = uploadsRoot.resolve(user).resolve("incoming");
        long fileCount = 0, byteCount = 0;

        try(Stream<Path> listOfFiles = Files.walk(incomingDir)) {

            fileCount = listOfFiles
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException E) {
            System.out.println("Error!");
        }

        try(Stream<Path> listOfFiles = Files.walk(incomingDir)) {
            byteCount = listOfFiles
                    .filter(Files::isRegularFile)
                    .mapToLong(filePath -> {
                        long size = 0;
                        try{
                            size =  Files.size(filePath);
                        } catch (IOException E) {
                            System.out.println("Error!");
                        }

                        return size;
                    }).sum();

        } catch (IOException E) {
            System.out.println("Error!");
        }

        return new long[] {fileCount, byteCount};
    }

}
