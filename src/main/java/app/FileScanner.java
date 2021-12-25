package app;

import models.DataPoint;
import models.File;
import models.Video;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileScanner {

    // Extensions the scanner looks for
    private static List<String> extensions = Arrays.asList(new String[]{"mp4"});

    private static boolean running = false;

    /**
     * Perform a scan of the file root to
     * maintain index of all files
     * 1. Go over all existing files and verify them
     * 2. Add all missing files
     */
    public static void scan() {
        if (running) return;
        running = true;
        UI.action("Commencing file scan...");
        // Verify existing files
        UI.action("Verifying existing files...");
        List<File> indexedFiles = File.findAll();
        indexedFiles.stream().parallel().forEach((File file) -> {
            verifyFile(file);
        });
        UI.action("Looking for new files...");
        // Add missing files
        List<String> indexedPaths = File.findAll().stream().map(file -> file.getPath()).collect(Collectors.toList());
        List<java.io.File> files = fetchFiles(new java.io.File(Settings.root));
        files.stream().parallel().forEach((java.io.File file) -> {
            if (!indexedPaths.contains(file.toString())) {
                handleFile(file);
            }
        });
        UI.action("File scan complete.");
        running = false;
    }

    /**
     * List all files in the given directory
     * @return list of all files
     */
    private static List<java.io.File> fetchFiles(java.io.File dir) {
        List<java.io.File> files = new ArrayList<>();
        // Get the files in the current directory
        java.io.File dirFiles[] = dir.listFiles();
        // And iterate
        for(java.io.File file : dirFiles) {
            // If its a file, add it, otherwise recurse
            if (file.isFile()) {
                files.add(file);
            } else {
                files.addAll(fetchFiles(file));
            }
        }
        return files;
    }

    /**
     * Handle a file scanned from the root media directory
     * @param file the file to handle
     */
    private static void handleFile(java.io.File file) {
        // We only care about some extensions
        if (!extensions.contains(FilenameUtils.getExtension(file.getPath()).toLowerCase())) {
            return;
        }
        File f = null;
        try {
            f = new File(file.toPath());
            f.insert();
            Video v = new Video(f);
            if (!v.isDjiVideo) return;
            v.insert();
            List<DataPoint> points = DataPoint.parseVideo(v);
            for (DataPoint point : points) {
                point.insert();
            }
            UI.action("Successfully parsed: " + f.getPath());
        } catch (IOException e) {
            System.err.println("ERROR: Unable to create file from " + file + ".");
            e.printStackTrace();
        } catch (SQLException throwables) {
            System.err.println("ERROR: Unable to insert model.");
            throwables.printStackTrace();
        }
    }

    /**
     * Verify the existence of a indexed file
     * if non existent, we delete
     * @param file to verify
     */
    public static void verifyFile(File file) {
        java.io.File f = new java.io.File(file.getPath());
        if (f.exists()) {
            return;
        } else {
            try {
                file.delete();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

}
