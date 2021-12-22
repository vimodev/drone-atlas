import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

import java.sql.SQLException;
import java.util.List;

public class App {

    public void run() throws ManagedProcessException {
        System.out.println("Hello world!");
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0);
        DB db = DB.newEmbeddedDB(config.build());
        db.start();
        String dbName = "drone_atlas";
        db.createDB(dbName);
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(config.getURL(dbName), "root", "");
            QueryRunner qr = new QueryRunner();

            // Should be able to create a new table
            qr.update(conn, "CREATE TABLE hello(world VARCHAR(100))");

            // Should be able to insert into a table
            qr.update(conn, "INSERT INTO hello VALUES ('Hello, world')");

            // Should be able to select from a table
            List<String> results = qr.query(conn, "SELECT * FROM hello",
                    new ColumnListHandler<String>());

            System.out.println(results);

        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    public static void main(String[] args) {
        App main = new App();
        try {
            main.run();
        } catch (ManagedProcessException e) {
            System.err.println("ERROR: Unable to initialize the embedded database");
            e.printStackTrace();
        }
    }

}
