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
        // Go over all files, multithreaded
        files.stream().parallel().forEach((java.io.File file) -> {
            // Add it to the database if its extension is good
            if (!extensions.contains(FilenameUtils.getExtension(file.getPath()))) {
                return;
            }
            File f = null;
            try {
                f = new File(file.toPath());
                f.insert();
                System.out.println("Successfully added " + file);
            } catch (IOException e) {
                System.err.println("ERROR: Unable to create file from " + file + ".");
                e.printStackTrace();
            } catch (SQLException throwables) {
                System.err.println("ERROR: Unable to insert File model " + f + ".");
                throwables.printStackTrace();
            }
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

}
