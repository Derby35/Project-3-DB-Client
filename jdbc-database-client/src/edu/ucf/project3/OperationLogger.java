package edu.ucf.project3;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class OperationLogger {
    public static void log(String type, String username) {
        try {
            Properties p = new Properties();
            p.load(new FileInputStream("config/project3app.properties"));

            Class.forName(p.getProperty("jdbc.driver"));
            try (Connection conn = DriverManager.getConnection(
                    p.getProperty("jdbc.url"),
                    p.getProperty("user"),
                    p.getProperty("password"))) {

                String col = type.equals("query") ? "num_queries" : "num_updates";
                String update = "UPDATE operationscount SET " + col + " = " + col + " + 1 WHERE login_username = ?";
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setString(1, username);
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        String insert = "INSERT INTO operationscount(login_username,num_queries,num_updates) VALUES(?,?,?)";
                        try (PreparedStatement ips = conn.prepareStatement(insert)) {
                            ips.setString(1, username);
                            ips.setInt(2, type.equals("query") ? 1 : 0);
                            ips.setInt(3, type.equals("query") ? 0 : 1);
                            ips.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Logger error: " + e.getMessage());
        }
    }
}
