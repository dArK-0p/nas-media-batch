package com.darkop.nas.app;

import com.darkop.nas.fs.UploadScanner;
import com.darkop.nas.fs.UploadSummary;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("NAS Batch Job starting...");
        String path = args[0];

        try{
            Path uploadsRoot = Paths.get(path);
            boolean validPath = Files.exists(uploadsRoot) && Files.isDirectory(uploadsRoot);
            if (!validPath) throw new Exception("Path does not exist or is not a directory.");
            List<UploadSummary> us = new UploadScanner(uploadsRoot).getUploadSummary();
            us.forEach(System.out::println);
        } catch(InvalidPathException E) {
            System.out.println("Invalid Path. Could not convert.\nExiting...");
        } catch (Exception E) {
            System.out.println(E.getMessage() + "\nPath provided = " + path);
        }
    }
}
