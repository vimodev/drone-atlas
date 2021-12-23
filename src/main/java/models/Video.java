package models;

import app.App;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Internal representation of a video
 */
public class Video {

    private String id;
    private String fileId;
    private File file;
    private int width;
    private int height;
    private double duration;
    private double fps;
    private int bitrate;

    /**
     * Find a video in the database by its id
     * @param id of the video
     * @return the Video or null
     */
    public static Video find(String id) {
        QueryRunner qr = new QueryRunner();
        ResultSetHandler<Video> resultHandler = new BeanHandler<>(Video.class);
        Video video = null;
        try {
            video = qr.query(App.conn, "SELECT * FROM videos WHERE id=?", resultHandler, id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        return video;
    }

    /**
     * Retrieve a list of all videos in the database
     * @return List of Video objects
     */
    public static List<Video> findAll() {
        QueryRunner qr = new QueryRunner();
        ResultSetHandler<List<Video>> resultHandler = new BeanListHandler<>(Video.class);
        List<Video> videos = null;
        try {
            videos = qr.query(App.conn, "SELECT * FROM videos", resultHandler);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        return videos;
    }

    /**
     * Probe the given file using ffprobe
     * and use the data to construct a Video object
     * @param file video is stored in
     */
    public Video(File file) throws IOException {
        this.id = UUID.randomUUID().toString();
        this.file = file;
        this.fileId = file.getId();
        FFmpegProbeResult result = new FFprobe(App.ffprobe).probe(file.getPath());
        FFmpegStream data = result.getStreams().get(0);
        this.width = data.width;
        this.height = data.height;
        this.duration = result.getFormat().duration;
        this.fps = data.avg_frame_rate.doubleValue();
        this.bitrate = (int) result.getFormat().bit_rate;
//        FFprobeResult ffprobe = FFprobe.atPath()
//                .setShowStreams(true)
//                .setInput(file.getPath())
//                .setLogLevel(LogLevel.QUIET)
//                .execute();
//        Stream data = ffprobe.getStreams().get(0);
//        this.width = data.getWidth();
//        this.height = data.getHeight();
//        this.duration = data.getDuration();
//        this.fps = data.getAvgFrameRate().doubleValue();
//        this.bitrate = data.getBitRate();
    }

    public Video() {}

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

    /**
     * Load the file if it is not loaded yet
     * @return File object
     */
    public File preloadFile() {
        if (this.file != null) return this.file;
        if (this.fileId == null) return null;
        File file = File.find(this.fileId);
        this.file = file;
        return file;
    }

    /**
     * Dump this video's subtitles to a temporary
     * file that is deleted when Java terminates
     * @return subtitle file
     */
    public java.io.File dumpSubtitles() throws IOException {
        if (this.file == null) preloadFile();
        if (this.file == null) return null;
        java.io.File output = java.io.File.createTempFile("drone-atlas-", ".srt");
        output.deleteOnExit();
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(this.file.getPath())
                .addOutput(output.getPath())
                .done();
        FFmpegExecutor exec = new FFmpegExecutor(new FFmpeg(App.ffmpeg));
        exec.createJob(builder).run();
        return output;
    }

    public String toString() {
        return id + ", " + (fileId != null ? fileId : file.getId()) + ", " + width + ", "
                + height + ", " + duration + ", " + fps + ", " + bitrate;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("fileId", file == null ? fileId : file.getId());
        json.put("width", width);
        json.put("height", height);
        json.put("duration", duration);
        json.put("fps", fps);
        json.put("bitrate", bitrate);
        return json;
    }

    public String getId() {
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

    public String getFileId() {
        return fileId;
    }

    public double getDuration() {
        return duration;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setFps(double fps) {
        this.fps = fps;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }
}
