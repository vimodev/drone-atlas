package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UI {

    public static JFrame management;
    public static GridLayout layout;
    public static JLabel webStatus;
    public static JLabel action;
    public static JButton sync;

    public static void createManagementUI() {
        management = new JFrame("Drone-Atlas management");
        management.setSize(500, 150);
        management.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        layout = new GridLayout(0, 1, 5, 5);
        management.setLayout(layout);
        webStatus = new JLabel("Web server: ", SwingConstants.CENTER);
        management.add(webStatus);
        action = new JLabel("", SwingConstants.CENTER);
        management.add(action);
        sync = new JButton("Check files");
        sync.addActionListener(actionEvent -> FileScanner.scan());
        management.add(sync);
        management.setVisible(true);
    }

    public static void setWebStatus(String status) {
        System.out.println("Web server: " + status + ".");
        webStatus.setText("Web server: " + status + ".");
    }

    public static void action(String action) {
        System.out.println(action);
        UI.action.setText(action);
    }

}
