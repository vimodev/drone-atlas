package app;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

public class App {

    // Webserver variables
    public static int webPort = 8080;

    // Embedded database variables
    public static DB db = null;
    public static DBConfigurationBuilder cfgb = null;
    public static String dbName = "drone_atlas";

    // Database connection variables
    public static Connection conn = null;

    public static java.io.File fileRoot = null;

    /**
     * Initialize the embedded database systems
     * @throws ManagedProcessException if it fails
     */
    private void initializeDatabaseServer() throws ManagedProcessException {
        cfgb = DBConfigurationBuilder.newBuilder();
        cfgb.setPort(0);
        db = DB.newEmbeddedDB(cfgb.build());
        db.start();
        db.createDB(dbName);
    }

    /**
     * Run the application
     */
    public void run() {
        // Start the Jetty web server
        try {
            JettyServer.start(webPort);
        } catch (Exception e) {
            System.err.println("ERROR: Unable to start Jetty webserver.");
            e.printStackTrace();
            System.exit(1);
        }
        // Attempt to open the file root
        fileRoot = new java.io.File("/home/vincent/Videos/Footage/20211216 Windmolen");
        // Set up embedded database server first
        try {
            initializeDatabaseServer();
        } catch (ManagedProcessException e) {
            System.err.println("ERROR: Unable to initialize embedded database server.");
            e.printStackTrace();
            System.exit(1);
        }
        // Set up MySQL connection driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: Unable to locate MySQL driver.");
            e.printStackTrace();
            System.exit(1);
        }
        // Connect to the database
        try {
            conn = DriverManager.getConnection(cfgb.getURL(dbName), "root", "");
        } catch (SQLException throwables) {
            System.err.println("ERROR: Unable to connect to embedded database.");
            throwables.printStackTrace();
            System.exit(1);
        }
        // Create the tables
        try {
            Migrator.createTables();
        } catch (SQLException throwables) {
            System.err.println("ERROR: Unable to create one or more tables.");
            throwables.printStackTrace();
            System.exit(1);
        }
        FileScanner.scan();
    }

    public static void main(String[] args) {
        App main = new App();
        main.run();
    }

}
