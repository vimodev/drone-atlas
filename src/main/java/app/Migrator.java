package app;

import app.App;
import org.apache.commons.dbutils.QueryRunner;

import java.sql.SQLException;

public class Migrator {

    public static void createTables() throws SQLException {
        createFilesTable();
        createVideosTable();
        createDataPointsTable();
        createSettingsTable();
    }

    public static void createFilesTable() throws SQLException {
        QueryRunner qr = new QueryRunner();
        String query = new StringBuilder()
                .append("CREATE TABLE files (")
                .append("id VARCHAR(255) PRIMARY KEY,")
                .append("path VARCHAR(255) NOT NULL,")
                .append("hash VARCHAR(255) NOT NULL,")
                .append("size BIGINT(8) NOT NULL")
                .append(")")
                .toString();
        qr.update(App.conn, query);
    }

    public static void createVideosTable() throws SQLException {
        QueryRunner qr = new QueryRunner();
        String query = new StringBuilder()
                .append("CREATE TABLE videos (")
                .append("id VARCHAR(255) PRIMARY KEY,")
                .append("fileId VARCHAR(255) NOT NULL,")
                .append("width int NOT NULL,")
                .append("height int NOT NULL,")
                .append("duration double NOT NULL,")
                .append("fps double NOT NULL,")
                .append("bitrate int NOT NULL,")
                .append("FOREIGN KEY (fileId) REFERENCES files(id) ON DELETE CASCADE")
                .append(")")
                .toString();
        qr.update(App.conn, query);
    }

    public static void createDataPointsTable() throws SQLException {
        QueryRunner qr = new QueryRunner();
        String query = new StringBuilder()
                .append("CREATE TABLE data_points (")
                .append("id VARCHAR(255) PRIMARY KEY,")
                .append("videoId VARCHAR(255) NOT NULL,")
                .append("sequenceNumber int NOT NULL,")
                .append("startSeconds double NOT NULL,")
                .append("focalLength double NOT NULL,")
                .append("shutterSpeed double NOT NULL,")
                .append("iso int NOT NULL,")
                .append("ev double NOT NULL,")
                .append("digitalZoom double NOT NULL,")
                .append("longitude double NOT NULL,")
                .append("latitude double NOT NULL,")
                .append("gpsSatCount int NOT NULL,")
                .append("distance double NOT NULL,")
                .append("height double NOT NULL,")
                .append("horizontalSpeed double NOT NULL,")
                .append("verticalSpeed double NOT NULL,")
                .append("FOREIGN KEY (videoId) REFERENCES videos(id) ON DELETE CASCADE")
                .append(")")
                .toString();
        qr.update(App.conn, query);
    }

    public static void createSettingsTable() throws SQLException {
        QueryRunner qr = new QueryRunner();
        String query = new StringBuilder()
                .append("CREATE TABLE settings (")
                .append("name VARCHAR(255) PRIMARY KEY,")
                .append("value TEXT")
                .append(")")
                .toString();
        qr.update(App.conn, query);
    }

}
