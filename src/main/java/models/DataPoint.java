package models;

import app.App;
import org.apache.commons.dbutils.QueryRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Internal representation of a data point in a video
 */
public class DataPoint {

    private String id;
    private Video video;
    private String videoId;
    private int sequenceNumber;
    private double startSeconds;
    private double focalLength;
    private double shutterSpeed;
    private int iso;
    private double ev;
    private double digitalZoom;
    private double longitude;
    private double latitude;
    private int gpsSatCount;
    private double distance;
    private double height;
    private double horizontalSpeed;
    private double verticalSpeed;

    public DataPoint(Video video, int sequenceNumber, double startSeconds, double focalLength, double shutterSpeed,
                     int iso, double ev, double digitalZoom, double longitude, double latitude, int gpsSatCount,
                     double distance, double height, double horizontalSpeed, double verticalSpeed) {
        this.id = UUID.randomUUID().toString();
        this.video = video;
        this.videoId = video.getId();
        this.sequenceNumber = sequenceNumber;
        this.startSeconds = startSeconds;
        this.focalLength = focalLength;
        this.shutterSpeed = shutterSpeed;
        this.iso = iso;
        this.ev = ev;
        this.digitalZoom = digitalZoom;
        this.longitude = longitude;
        this.latitude = latitude;
        this.gpsSatCount = gpsSatCount;
        this.distance = distance;
        this.height = height;
        this.horizontalSpeed = horizontalSpeed;
        this.verticalSpeed = verticalSpeed;
    }

    /**
     * Get a list of DataPoints from a Video
     * @param video object
     * @return list of DataPoints
     */
    public static List<DataPoint> fromVideo(Video video) throws IOException {
        // Fetch the subtitle file from the video
        java.io.File subtitles = video.dumpSubtitles();
        // Read relevant lines
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(subtitles))) {
            int lineNumber = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (lineNumber % 5 < 3) lines.add(line);
                lineNumber++;
            }
        }
        // We must have that the lines are now in groups of 3, otherwise
        // something must have gone wrong
        assert lines.size() % 3 == 0;
        // Parse the lines and fill the list of DataPoints
        List<DataPoint> list = new ArrayList<>();
        for (int s = 0; s < lines.size() / 3; s ++) {
            // Get the individual lines
            String first = lines.get(s * 3);
            String second = lines.get(s * 3 + 1);
            String third = lines.get(s * 3 + 2);
            String[] meta = third.split(", ");
            // Get the starting time of the data point
            String[] components = second.split(" ")[0].split(":");
            double startSeconds = 360 * Double.parseDouble(components[0]) +
                                    60 * Double.parseDouble(components[1]) +
                                    Double.parseDouble(components[2].replace(',', '.'));
            // Get the data from the third line
            // Example segment:
            // 5
            // 00:00:04,000 --> 00:00:05,000
            // F/2.8, SS 1000.82, ISO 100, EV 0, DZOOM 1.000, GPS (7.7298, 5.2099, 23), D 9.91m, H 1.60m, H.S 9.45m/s, V.S 1.90m/s
            //
            double focalLength = Double.parseDouble(meta[0].split("/")[1].trim());
            double shutterspeed = Double.parseDouble(meta[1].trim().split(" ")[1]);
            int iso = Integer.parseInt(meta[2].trim().split(" ")[1]);
            double ev = Double.parseDouble(meta[3].trim().split(" ")[1]);
            double dzoom = Double.parseDouble(meta[4].trim().split(" ")[1]);
            double longitude = Double.parseDouble(meta[5].split("\\(")[1]);
            double latitude = Double.parseDouble(meta[6].trim());
            int gpsSatCount = Integer.parseInt(meta[7].trim().split("\\)")[0]);
            double distance = Double.parseDouble(meta[8].trim().split(" ")[1].replace("m",""));
            double height = Double.parseDouble(meta[9].trim().split(" ")[1].replace("m",""));
            double horizontalSpeed = Double.parseDouble(meta[10].trim().split(" ")[1].replace("m/s", ""));
            double verticalSpeed = Double.parseDouble(meta[11].trim().split(" ")[1].replace("m/s", ""));
            // Create a point from the data
            DataPoint point = new DataPoint(video, s + 1, startSeconds, focalLength, shutterspeed,
                    iso, ev, dzoom, longitude, latitude, gpsSatCount, distance, height, horizontalSpeed, verticalSpeed);
            list.add(point);
        }
        return list;
    }

    /**
     * Save this datapoint object to the database
     * by inserting it
     * @throws SQLException if something goes wrong
     */
    public void insert() throws SQLException {
        QueryRunner qr = new QueryRunner();
        String query = new StringBuilder()
                .append("INSERT INTO data_points ")
                .append("VALUES (")
                .append("'" + id + "',")
                .append("'" + video.getId() + "',")
                .append(sequenceNumber + ",").append(startSeconds + ",").append(focalLength + ",")
                .append(shutterSpeed + ",").append(iso + ",").append(ev + ",")
                .append(digitalZoom + ",").append(longitude + ",").append(latitude + ",")
                .append(gpsSatCount + ",").append(distance + ",").append(height + ",")
                .append(horizontalSpeed + ",").append(verticalSpeed)
                .append(")")
                .toString();
        qr.update(App.conn, query);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public double getStartSeconds() {
        return startSeconds;
    }

    public void setStartSeconds(double startSeconds) {
        this.startSeconds = startSeconds;
    }

    public double getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(double focalLength) {
        this.focalLength = focalLength;
    }

    public double getShutterSpeed() {
        return shutterSpeed;
    }

    public void setShutterSpeed(double shutterSpeed) {
        this.shutterSpeed = shutterSpeed;
    }

    public int getIso() {
        return iso;
    }

    public void setIso(int iso) {
        this.iso = iso;
    }

    public double getEv() {
        return ev;
    }

    public void setEv(double ev) {
        this.ev = ev;
    }

    public double getDigitalZoom() {
        return digitalZoom;
    }

    public void setDigitalZoom(double digitalZoom) {
        this.digitalZoom = digitalZoom;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getGpsSatCount() {
        return gpsSatCount;
    }

    public void setGpsSatCount(int gpsSatCount) {
        this.gpsSatCount = gpsSatCount;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getHorizontalSpeed() {
        return horizontalSpeed;
    }

    public void setHorizontalSpeed(double horizontalSpeed) {
        this.horizontalSpeed = horizontalSpeed;
    }

    public double getVerticalSpeed() {
        return verticalSpeed;
    }

    public void setVerticalSpeed(double verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }
}
