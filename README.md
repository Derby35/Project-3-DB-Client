# JDBC Database Client

Course: CNT 4714 - Enterprise Computing
Assignment: Project 3
Date: Summer 2025

## Description

Two Java Swing GUI applications for interacting with a MySQL database over JDBC. The main client (`Project3App`) supports both SELECT queries and update statements and can connect to any configured database using credential files. A second restricted client (`AccountantApp`) limits access to SELECT queries only. All executed operations are logged to a separate operations tracking database.

## Applications

**Project3App** - General-purpose SQL client
- Connects to any database listed in a properties file
- Supports SELECT queries and INSERT/UPDATE/DELETE statements
- Displays query results in a scrollable table
- Logs each query and update to the `operationslog` database

**AccountantApp** - Read-only SQL client
- Restricted to SELECT statements only
- Uses a fixed credential file for the accountant user

## Technologies Used

- Java (Swing / AWT)
- JDBC (MySQL Connector/J)
- MySQL

## Database Setup

This project requires a running MySQL instance with the following databases:

- `bikedb` or `project3` - target data databases (created externally)
- `operationslog` - tracks query/update counts per user; must contain an `operationscount` table:

```sql
CREATE DATABASE operationslog;
USE operationslog;
CREATE TABLE operationscount (
    login_username VARCHAR(50) PRIMARY KEY,
    num_queries    INT DEFAULT 0,
    num_updates    INT DEFAULT 0
);
```

Update `config/root.properties` with your local MySQL root password before connecting as root.

## Project Structure

```
jdbc-database-client/
├── src/edu/ucf/project3/   Java source files
├── config/                  Connection and credential properties files
└── README.md
```

## Config Files

| File | Purpose |
|---|---|
| `bikedb.properties` | JDBC URL for the bikedb database |
| `project3.properties` | JDBC URL for the project3 database |
| `client1.properties` | Credentials for client1 user |
| `client2.properties` | Credentials for client2 user |
| `root.properties` | Credentials for root user (update password) |
| `theaccountant.properties` | Credentials and connection for accountant user |
| `project3app.properties` | Credentials used by the operations logger |

## How to Run

Place the MySQL Connector/J JAR in a `lib/` directory, then compile and run from the project root:

**Compile:**
```bash
javac -cp lib/mysql-connector-j-*.jar -d bin src/edu/ucf/project3/*.java
```

**Run Project3App:**
```bash
java -cp "bin;lib/mysql-connector-j-*.jar" edu.ucf.project3.Project3App
```

**Run AccountantApp:**
```bash
java -cp "bin;lib/mysql-connector-j-*.jar" edu.ucf.project3.AccountantApp
```

On Linux/macOS, replace `;` with `:` in the classpath.

## Usage

1. Select a database properties file and a user properties file from the dropdowns
2. Enter your username and password
3. Click Connect
4. Type a SQL command in the input area and click Execute
5. Results appear in the table below; row counts are shown for non-SELECT statements
