package app;

import java.io.*;

import static app.App.ffmpeg;
import static app.App.ffprobe;

public class Utility {

    /**
     * Extract a resource to the host, which is deleted on exit.
     * @param resource to extract
     * @return the resulting extracted File
     * @throws IOException if something goes wrong
     */
    public static java.io.File extractResource(String resource) throws IOException {
        InputStream is = Utility.class.getClassLoader().getResource(resource).openStream();
        java.io.File output = java.io.File.createTempFile("drone-atlas-extract-", "");
        output.deleteOnExit();
        OutputStream os = new FileOutputStream(output);
        byte[] b = new byte[2048];
        int length;
        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }
        is.close();
        os.close();
        return output;
    }

    /**
     * Set the paths to the extracted ffmpeg executables.
     * @throws IOException
     */
    public static void setFfPaths() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            java.io.File mpeg = Utility.extractResource("executables/linux/ffmpeg");
            ffmpeg = mpeg.getAbsolutePath();
            java.io.File probe = Utility.extractResource("executables/linux/ffprobe");
            ffprobe = probe.getAbsolutePath();
            Runtime.getRuntime().exec("chmod +x " + ffmpeg);
            Runtime.getRuntime().exec("chmod +x " + ffprobe);
        } else if (os.contains("windows")) {
            java.io.File mpeg = Utility.extractResource("executables/windows/ffmpeg.exe");
            ffmpeg = mpeg.getAbsolutePath();
            java.io.File probe = Utility.extractResource("executables/windows/ffprobe.exe");
            ffprobe = probe.getAbsolutePath();
        }
    }

}
