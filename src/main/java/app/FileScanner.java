package app;

import app.App;
import models.DataPoint;
import models.File;
import models.Video;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileScanner {

    // Extensions the scanner looks for
    private static List<String> extensions = Arrays.asList(new String[]{"mp4"});

    /**
     * Perform a scan of the file root to
     * maintain index of all files
     */
    public static void scan() {
        // Fetch all the files
        List<java.io.File> files = fetchFiles(App.fileRoot);
        System.out.println(files);
        // Go over all files, multithreaded
        files.stream().parallel().forEach((java.io.File file) -> {
            handleFile(file);
        });
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
            System.out.println(f.toJSON());
            Video v = new Video(f);
            v.insert();
            System.out.println(v.toJSON());
            List<DataPoint> points = DataPoint.fromVideo(v);
            for (DataPoint point : points) {
                point.insert();
            }
        } catch (IOException e) {
            System.err.println("ERROR: Unable to create file from " + file + ".");
            e.printStackTrace();
        } catch (SQLException throwables) {
            System.err.println("ERROR: Unable to insert model.");
            throwables.printStackTrace();
        }
    }

}
