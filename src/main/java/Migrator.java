import org.apache.commons.dbutils.QueryRunner;

import java.sql.SQLException;

public class Migrator {

    public static void createTables() throws SQLException {
        createFilesTable();
        createVideosTable();
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
                .append("file_id VARCHAR(255) NOT NULL,")
                .append("width int NOT NULL,")
                .append("height int NOT NULL,")
                .append("duration double NOT NULL,")
                .append("fps double NOT NULL,")
                .append("bitrate int NOT NULL,")
                .append("FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE")
                .append(")")
                .toString();
        qr.update(App.conn, query);
    }

}
