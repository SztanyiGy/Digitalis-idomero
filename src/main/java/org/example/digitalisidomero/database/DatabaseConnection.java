package org.example.digitalisidomero.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:timetracker.db";
    private static DatabaseConnection instance;
    private Connection connection;

    // Privát konstruktor (Singleton pattern)
    private DatabaseConnection() {
        try {
            // SQLite JDBC driver betöltése
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_URL);
            System.out.println("✓ Adatbázis kapcsolat létrehozva: " + DB_URL);
        } catch (ClassNotFoundException e) {
            System.err.println("✗ SQLite JDBC driver nem található!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Adatbázis kapcsolat hiba!");
            e.printStackTrace();
        }
    }

    // Singleton instance lekérése
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    // Connection lekérése
    public Connection getConnection() {
        try {
            // Ha lezárult vagy null, újra létrehozzuk
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("✗ Kapcsolat ellenőrzési hiba!");
            e.printStackTrace();
        }
        return connection;
    }

    // Kapcsolat lezárása
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Adatbázis kapcsolat lezárva.");
            }
        } catch (SQLException e) {
            System.err.println("✗ Kapcsolat lezárási hiba!");
            e.printStackTrace();
        }
    }
}