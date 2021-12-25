package app;

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.apache.commons.dbutils.QueryRunner;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

public class App {

    public static String ffmpeg = "";
    public static String ffprobe = "";
    public static String workingDir = "";
    public static String sqlName = "data";

    // Webserver variables
    public static int webPort = 0;

    // Embedded database variables
    public static DB db = null;
    public static DBConfigurationBuilder cfgb = null;
    public static String dbName = "drone_atlas";
    public boolean newDatabase = false;

    // Database connection variables
    public static Connection conn = null;

    public static java.io.File fileRoot = null;

    /**
     * Initialize the embedded database systems
     * @throws ManagedProcessException if it fails
     */
    private void initializeDatabaseServer() {
        try {
            cfgb = DBConfigurationBuilder.newBuilder();
            cfgb.setPort(0);
            cfgb.setDataDir(Paths.get(workingDir, sqlName).toString());
            db = DB.newEmbeddedDB(cfgb.build());
            db.start();
        } catch (ManagedProcessException e) {
            System.err.println("ERROR: Unable to initialize embedded database server.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Try to connect to the database,
     * if it does not work, try to create it and migrate it.
     */
    private void connectDatabase() {
        // Load the connection driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: Unable to locate MySQL driver.");
            e.printStackTrace();
            System.exit(1);
        }
        // Try to connect
        try {
            conn = DriverManager.getConnection(cfgb.getURL(dbName), "root", "");
        } catch (SQLException throwables) {
            // If it fails we assume its because its a new folder, so no database yet
            System.out.println("Did not find database, creating.");
            newDatabase = true;
            JOptionPane.showMessageDialog(new JDialog(), "No previous data detected. You may be required to configure some settings.");
            // And we create it
            try {
                db.createDB(dbName);
                conn = DriverManager.getConnection(cfgb.getURL(dbName), "root", "");
                createTables();
            } catch (ManagedProcessException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Populate the database with the tables we require
     */
    private void createTables() {
        try {
            Migrator.createTables();
        } catch (SQLException throwables) {
            System.err.println("ERROR: Unable to create one or more tables.");
            throwables.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Open the user's browser to expose the web UI
     */
    private void openBrowser() {
        int port = JettyServer.connector.getLocalPort();
        try {
            UI.action("Opening browser at http://localhost:" + port);
            Desktop.getDesktop().browse(new URI("http://localhost:" + port));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }



    /**
     * Set the working dir variable based on currently running jar
     */
    private void setWorkingDir() {
        try {
            workingDir = new java.io.File(App.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Extract the packaged native binaries for use
     */
    private void extractBinaries() {
        try {
            UI.action("Extracting native ffmpeg executables.");
            Utility.setFfPaths();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Start the Jetty webserver
     */
    private void startJetty() {
        // Start the Jetty web server
        try {
            JettyServer.start(webPort);
        } catch (Exception e) {
            System.err.println("ERROR: Unable to start Jetty webserver.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Run the application
     */
    public void run() {
        UI.createManagementUI();
        UI.setWebStatus("Stopped");
        setWorkingDir();
        extractBinaries();
        UI.setWebStatus("Starting");
        startJetty();
        UI.setWebStatus("Running on http://localhost:" + JettyServer.connector.getLocalPort());
        initializeDatabaseServer();
        connectDatabase();
        openBrowser();
        Settings.load();
        FileScanner.scan();
    }

    public static void close() {
        try { UI.action("Stopping webserver."); JettyServer.stop(); } catch (Exception e) { }
        try { UI.action("Closing db connection."); conn.close(); } catch (Exception e) { }
        try { UI.action("Stopping db server."); db.stop(); } catch (Exception e) { }
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> App.close()));
        App main = new App();
        main.run();
    }

}
