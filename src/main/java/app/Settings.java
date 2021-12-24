package app;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

public class Settings {

    public static String root;

    public static void load() {
        loadRoot();
    }

    /**
     * Try to load root setting from database
     * otherwise ask user to specify and save it
     */
    public static void loadRoot() {
        QueryRunner qr = new QueryRunner();
        try {
            List<String> result = qr.query(App.conn, "SELECT value FROM settings WHERE name=?", new ColumnListHandler<>(), "root");
            if (result == null || result.size() == 0) {
                System.out.println("Missing root setting, prompting user.");
                setRoot();
            } else {
                root = result.get(0);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.exit(1);
        }
    }

    public static void setRoot() {
        java.io.File fileRoot = promptFileRoot();
        root = fileRoot.toString();
        QueryRunner qr = new QueryRunner();
        try {
            qr.update(App.conn, "INSERT INTO settings VALUES (?,?)", "root", root);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Prompt the user to specify the file root
     */
    private static java.io.File promptFileRoot() {
        // Ask user for the file root
        JOptionPane.showMessageDialog(new JDialog(), "Please select the directory where your footage is located.");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(new JDialog());
        if (result != JFileChooser.APPROVE_OPTION) {
            System.exit(1);
        }
        return fileChooser.getSelectedFile();
    }

}