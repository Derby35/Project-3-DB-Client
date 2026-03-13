// src/edu/ucf/project3/AccountantApp.java
package edu.ucf.project3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class AccountantApp {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextArea sqlInput;
    private JTable resultTable;
    private JLabel statusLabel;
    private Connection conn;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AccountantApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Accountant SQL Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout(10, 10));

        // Top panel: login
        JPanel loginPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Login"));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);

        // SQL input
        sqlInput = new JTextArea(4, 80);
        JScrollPane inputScroll = new JScrollPane(sqlInput);
        inputScroll.setBorder(BorderFactory.createTitledBorder("SQL Command"));

        // Results
        resultTable = new JTable();
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setAutoCreateRowSorter(true);
        JScrollPane resultScroll = new JScrollPane(resultTable);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Results"));
        resultScroll.setPreferredSize(new Dimension(800, 300));

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton connectBtn = new JButton("Connect");
        JButton execBtn = new JButton("Execute");
        JButton clearSQL = new JButton("Clear SQL");
        JButton clearResults = new JButton("Clear Results");
        JButton exitBtn = new JButton("Exit");

        buttonPanel.add(connectBtn);
        buttonPanel.add(execBtn);
        buttonPanel.add(clearSQL);
        buttonPanel.add(clearResults);
        buttonPanel.add(exitBtn);

        statusLabel = new JLabel("Disconnected");

        frame.add(loginPanel, BorderLayout.NORTH);
        frame.add(inputScroll, BorderLayout.CENTER);
        frame.add(resultScroll, BorderLayout.SOUTH);
        frame.add(buttonPanel, BorderLayout.PAGE_END);
        frame.add(statusLabel, BorderLayout.WEST);

        // Button logic
        connectBtn.addActionListener(e -> connect());
        execBtn.addActionListener(e -> executeQuery());
        clearSQL.addActionListener(e -> sqlInput.setText(""));
        clearResults.addActionListener(e -> resultTable.setModel(new DefaultTableModel()));
        exitBtn.addActionListener(e -> {
            disconnect();
            System.exit(0);
        });

        frame.setVisible(true);
    }

    private void connect() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config/theaccountant.properties"));

            String inputUser = usernameField.getText().trim();
            String inputPass = new String(passwordField.getPassword());
            String fileUser = props.getProperty("user").trim();
            String filePass = props.getProperty("password").trim();

            if (!inputUser.equals(fileUser) || !inputPass.equals(filePass)) {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Class.forName(props.getProperty("jdbc.driver"));
            conn = DriverManager.getConnection(props.getProperty("jdbc.url"), inputUser, inputPass);
            statusLabel.setText("Connected as " + inputUser);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Disconnected");
        }
    }

    private void disconnect() {
        try {
            if (conn != null) conn.close();
        } catch (Exception ignored) {}
        conn = null;
        statusLabel.setText("Disconnected");
    }

    private void executeQuery() {
        if (conn == null) {
            JOptionPane.showMessageDialog(frame, "Please connect first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = sqlInput.getText().trim();
        if (!sql.toLowerCase().startsWith("select")) {
            JOptionPane.showMessageDialog(frame, "Only SELECT queries are allowed.", "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            resultTable.setModel(new ResultSetTableModel(rs));
            statusLabel.setText("Last executed: " + sql.split("\n")[0]);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error");
        }
    }
}
