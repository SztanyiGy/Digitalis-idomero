package org.example.digitalisidomero.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (Statement stmt = conn.createStatement()) {

            // 1. Applications tábla létrehozása
            String createApplicationsTable = """
                CREATE TABLE IF NOT EXISTS applications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    display_name TEXT NOT NULL,
                    category TEXT NOT NULL
                );
                """;
            stmt.execute(createApplicationsTable);
            System.out.println("✓ Applications tábla létrehozva/ellenőrizve.");

            // 2. Sessions tábla létrehozása
            String createSessionsTable = """
                CREATE TABLE IF NOT EXISTS sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    application_id INTEGER NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time TEXT,
                    duration_seconds INTEGER DEFAULT 0,
                    date TEXT NOT NULL,
                    FOREIGN KEY (application_id) REFERENCES applications(id)
                );
                """;
            stmt.execute(createSessionsTable);
            System.out.println("✓ Sessions tábla létrehozva/ellenőrizve.");

            // 3. Index létrehozása a gyorsabb lekérdezésekhez
            String createDateIndex = """
                CREATE INDEX IF NOT EXISTS idx_sessions_date 
                ON sessions(date);
                """;
            stmt.execute(createDateIndex);

            String createAppIdIndex = """
                CREATE INDEX IF NOT EXISTS idx_sessions_app_id 
                ON sessions(application_id);
                """;
            stmt.execute(createAppIdIndex);
            System.out.println("✓ Indexek létrehozva.");

            System.out.println("✓ Adatbázis inicializálás sikeres!");

        } catch (SQLException e) {
            System.err.println("✗ Adatbázis inicializálási hiba!");
            e.printStackTrace();
        }
    }

    // Teszteléshez: adatbázis alaphelyzetbe állítása (óvatosan!)
    public static void resetDatabase() {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS sessions;");
            stmt.execute("DROP TABLE IF EXISTS applications;");
            System.out.println("✓ Adatbázis törölve.");

            // Újra létrehozás
            initializeDatabase();

        } catch (SQLException e) {
            System.err.println("✗ Adatbázis törlési hiba!");
            e.printStackTrace();
        }
    }
}