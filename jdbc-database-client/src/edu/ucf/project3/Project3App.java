// src/edu/ucf/project3/Project3App.java
package edu.ucf.project3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Project3App {
    private JFrame frame;
    private JComboBox<String> dbCombo, userCombo;
    private JTextField userField;
    private JPasswordField passField;
    private JTextArea sqlInput;
    private JTable resultTable;
    private JLabel statusLabel;
    private Connection conn;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Project3App().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Project 3 Client Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        JPanel topPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Connection"));

        topPanel.add(new JLabel("DB Properties:"));
        dbCombo = new JComboBox<>(listPropertiesFiles("config", ".properties", Arrays.asList("operationslog.properties")));
        topPanel.add(dbCombo);

        topPanel.add(new JLabel("User Properties:"));
        userCombo = new JComboBox<>(listPropertiesFiles("config", ".properties", Collections.emptyList()));
        topPanel.add(userCombo);

        topPanel.add(new JLabel("Username:"));
        userField = new JTextField();
        topPanel.add(userField);

        topPanel.add(new JLabel("Password:"));
        passField = new JPasswordField();
        topPanel.add(passField);

        JButton connectBtn = new JButton("Connect");
        connectBtn.addActionListener(e -> connectAction());
        topPanel.add(connectBtn);

        JButton disconnectBtn = new JButton("Disconnect");
        disconnectBtn.addActionListener(e -> disconnectAction());
        topPanel.add(disconnectBtn);

        statusLabel = new JLabel("Disconnected");
        topPanel.add(statusLabel);

        sqlInput = new JTextArea(5, 80);
        JScrollPane sqlScroll = new JScrollPane(sqlInput);
        sqlScroll.setBorder(BorderFactory.createTitledBorder("SQL Command"));

        resultTable = new JTable();
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setAutoCreateRowSorter(true);
        JScrollPane resultScroll = new JScrollPane(resultTable);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Results"));
        resultScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sqlScroll, resultScroll);
        splitPane.setResizeWeight(0.3);

        JButton execBtn = new JButton("Execute");
        execBtn.addActionListener(e -> executeSQL());
        JButton clearInputBtn = new JButton("Clear Input");
        clearInputBtn.addActionListener(e -> sqlInput.setText(""));
        JButton clearResultBtn = new JButton("Clear Results");
        clearResultBtn.addActionListener(e -> resultTable.setModel(new DefaultTableModel()));
        JButton exitBtn = new JButton("Exit");
        exitBtn.addActionListener(e -> {
            disconnectAction();
            System.exit(0);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(execBtn);
        buttonPanel.add(clearInputBtn);
        buttonPanel.add(clearResultBtn);
        buttonPanel.add(exitBtn);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.PAGE_END);

        frame.setVisible(true);
    }

    private String[] listPropertiesFiles(String dir, String suffix, List<String> exclude) {
        File d = new File(dir);
        String[] files = d.list((d1, name) -> name.endsWith(suffix) && !exclude.contains(name));
        return files != null ? files : new String[0];
    }

    private void connectAction() {
        if (conn != null) {
            JOptionPane.showMessageDialog(frame, "Already connected");
            return;
        }
        String dbProp = (String) dbCombo.getSelectedItem();
        String userProp = (String) userCombo.getSelectedItem();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        try {
            conn = DBUtil.verifyAndConnect("config/" + dbProp, "config/" + userProp, user, pass);
            statusLabel.setText("Connected as " + user);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Disconnected");
        }
    }

    private void disconnectAction() {
        if (conn != null) try { conn.close(); } catch (Exception ignored) {}
        conn = null;
        statusLabel.setText("Disconnected");
    }

    private void executeSQL() {
        if (conn == null) {
            JOptionPane.showMessageDialog(frame, "Not connected");
            return;
        }

        String sql = sqlInput.getText().trim();
        if (sql.isEmpty()) return;

        try {
            if (sql.toLowerCase().startsWith("select")) {
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    ResultSetTableModel model = new ResultSetTableModel(rs);
                    resultTable.setModel(model);
                    for (int i = 0; i < resultTable.getColumnCount(); i++) {
                        resultTable.getColumnModel().getColumn(i).setPreferredWidth(150);
                    }
                    OperationLogger.log("query", userField.getText().trim());
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int count = ps.executeUpdate();
                    JOptionPane.showMessageDialog(frame, count + " row(s) affected.");
                    OperationLogger.log("update", userField.getText().trim());
                }
            }
            statusLabel.setText("Last executed: " + sql.split("\\n")[0]);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error");
        }
    }
}
