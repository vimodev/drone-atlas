import org.apache.commons.dbutils.QueryRunner;

import java.sql.SQLException;

public class Migrator {

    public static void createTables() throws SQLException {
        createFilesTable();
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

}
