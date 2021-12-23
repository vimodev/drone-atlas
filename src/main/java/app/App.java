package app;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

public class App {

    // Webserver variables
    public static int webPort = 0;

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
        // Ask user for the file root
        JOptionPane.showMessageDialog(new JDialog(), "Please select the directory where your footage is located.");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(new JDialog());
        if (result != JFileChooser.APPROVE_OPTION) {
            System.exit(1);
        }
        fileRoot = fileChooser.getSelectedFile();
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
        int port = JettyServer.connector.getLocalPort();
        try {
            System.out.println("File scan commencing, please be patient. Opening browser at http://localhost:" + port);
            Desktop.getDesktop().browse(new URI("http://localhost:" + port));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        FileScanner.scan();
        System.out.println("File scan done.");
    }

    public static void main(String[] args) {
        App main = new App();
        main.run();
    }

}
