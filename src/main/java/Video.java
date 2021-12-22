import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Stream;
import org.apache.commons.dbutils.QueryRunner;

import java.sql.SQLException;
import java.util.UUID;

public class Video {

    private UUID id;
    private File file;
    private int width;
    private int height;
    private double duration;
    private double fps;
    private int bitrate;

    /**
     * Probe the given file using ffprobe
     * and use the data to construct a Video object
     * @param file video is stored in
     */
    public Video(File file) {
        this.id = UUID.randomUUID();
        this.file = file;
        FFprobeResult ffprobe = FFprobe.atPath()
                .setShowStreams(true)
                .setInput(file.getPath())
                .setLogLevel(LogLevel.QUIET)
                .execute();
        Stream data = ffprobe.getStreams().get(0);
        this.width = data.getWidth();
        this.height = data.getHeight();
        this.duration = data.getDuration();
        this.fps = data.getAvgFrameRate().doubleValue();
        this.bitrate = data.getBitRate();
    }

    /**
     * Save this Video object to the database
     * by inserting it as a new row
     * @throws SQLException if it fails
     */
    public void insert() throws SQLException {
        QueryRunner qr = new QueryRunner();
        String query = new StringBuilder()
                .append("INSERT INTO videos ")
                .append("VALUES (")
                .append("'" + id + "',")
                .append("'" + file.getId() + "',")
                .append(width + ",")
                .append(height + ",")
                .append(duration + ",")
                .append(fps + ",")
                .append(bitrate)
                .append(")")
                .toString();
        qr.update(App.conn, query);
    }

    public UUID getId() {
        return id;
    }

    public File getFile() {
        return file;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getFps() {
        return fps;
    }

    public int getBitrate() {
        return bitrate;
    }
}
