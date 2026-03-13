package edu.ucf.project3;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class DBUtil {
    public static Connection verifyAndConnect(String dbPropsFile, String userPropsFile,
                                              String username, String password) throws Exception {
        Properties dbp = new Properties();
        Properties up = new Properties();

        try (FileInputStream dbStream = new FileInputStream(dbPropsFile);
             FileInputStream userStream = new FileInputStream(userPropsFile)) {
            dbp.load(dbStream);
            up.load(userStream);
        }

        if (!username.equals(up.getProperty("user")) ||
            !password.equals(up.getProperty("password"))) {
            throw new Exception("Credentials mismatch");
        }

        Class.forName(dbp.getProperty("jdbc.driver"));
        return DriverManager.getConnection(dbp.getProperty("jdbc.url"), username, password);
    }
}
