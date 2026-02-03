package org.example.digitalisidomero.database.dao;

import org.example.digitalisidomero.database.DatabaseConnection;
import org.example.digitalisidomero.model.Category;
import org.example.digitalisidomero.model.Session;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Session mentése (INSERT) - KATEGÓRIÁVAL
    public int save(Session session) {
        String sql = "INSERT INTO sessions (application_id, start_time, end_time, duration_seconds, date, category) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, session.getApplicationId());
            pstmt.setString(2, session.getStartTime().format(DATETIME_FORMATTER));
            pstmt.setString(3, session.getEndTime() != null ? session.getEndTime().format(DATETIME_FORMATTER) : null);
            pstmt.setLong(4, session.getDurationSeconds());
            pstmt.setString(5, session.getStartTime().toLocalDate().format(DATE_FORMATTER));
            pstmt.setString(6, session.getCategory() != null ? session.getCategory().name() : Category.OTHER.name());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // SQLite-nál last_insert_rowid() használata
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        session.setId(id);
                        System.out.println("✓ Session mentve (ID: " + id + ", Duration: " + session.getDurationSeconds() + "s, Category: " + session.getCategory() + ")");
                        return id;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Session mentési hiba!");
            e.printStackTrace();
        }
        return -1;
    }

    // Session frissítése (UPDATE) - pl. lezáráskor
    public boolean update(Session session) {
        String sql = "UPDATE sessions SET end_time = ?, duration_seconds = ?, category = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, session.getEndTime() != null ? session.getEndTime().format(DATETIME_FORMATTER) : null);
            pstmt.setLong(2, session.getDurationSeconds());
            pstmt.setString(3, session.getCategory() != null ? session.getCategory().name() : Category.OTHER.name());
            pstmt.setInt(4, session.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✓ Session frissítve (ID: " + session.getId() + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("✗ Session frissítési hiba!");
            e.printStackTrace();
        }
        return false;
    }

    // Session keresése ID alapján
    public Session findById(int id) {
        String sql = "SELECT * FROM sessions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractSessionFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Session keresési hiba (ID: " + id + ")");
            e.printStackTrace();
        }
        return null;
    }

    // Egy adott nap összes session-je
    public List<Session> findByDate(LocalDate date) {
        String sql = "SELECT * FROM sessions WHERE date = ? ORDER BY start_time";
        List<Session> sessions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date.format(DATE_FORMATTER));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sessions.add(extractSessionFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Napi session-ök lekérési hiba: " + date);
            e.printStackTrace();
        }
        return sessions;
    }

    // Dátumtartomány szerinti session-ök (pl. heti/havi statisztikához)
    public List<Session> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM sessions WHERE date BETWEEN ? AND ? ORDER BY start_time";
        List<Session> sessions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.format(DATE_FORMATTER));
            pstmt.setString(2, endDate.format(DATE_FORMATTER));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sessions.add(extractSessionFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Tartomány session-ök lekérési hiba: " + startDate + " - " + endDate);
            e.printStackTrace();
        }
        return sessions;
    }

    // Egy alkalmazás összes session-je
    public List<Session> findByApplicationId(int applicationId) {
        String sql = "SELECT * FROM sessions WHERE application_id = ? ORDER BY start_time DESC";
        List<Session> sessions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, applicationId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sessions.add(extractSessionFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Alkalmazás session-ök lekérési hiba (App ID: " + applicationId + ")");
            e.printStackTrace();
        }
        return sessions;
    }

    // Összes session törlése egy adott dátum előtt (régi adatok tisztítása)
    public int deleteBeforeDate(LocalDate date) {
        String sql = "DELETE FROM sessions WHERE date < ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date.format(DATE_FORMATTER));
            int deletedRows = pstmt.executeUpdate();
            System.out.println("✓ " + deletedRows + " session törölve (" + date + " előtt)");
            return deletedRows;

        } catch (SQLException e) {
            System.err.println("✗ Session törlési hiba!");
            e.printStackTrace();
        }
        return 0;
    }

    // Egy session törlése
    public boolean delete(int id) {
        String sql = "DELETE FROM sessions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✓ Session törölve (ID: " + id + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("✗ Session törlési hiba (ID: " + id + ")");
            e.printStackTrace();
        }
        return false;
    }

    // Segédfüggvény: Session objektum létrehozása ResultSet-ből - KATEGÓRIÁVAL
    private Session extractSessionFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int applicationId = rs.getInt("application_id");

        String startTimeStr = rs.getString("start_time");
        String endTimeStr = rs.getString("end_time");

        LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DATETIME_FORMATTER);
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr, DATETIME_FORMATTER) : null;

        long durationSeconds = rs.getLong("duration_seconds");

        // Kategória beolvasása
        String categoryStr = rs.getString("category");
        Category category = categoryStr != null ? Category.valueOf(categoryStr) : Category.OTHER;

        Session session = new Session(id, applicationId, startTime, endTime, durationSeconds);
        session.setCategory(category);

        return session;
    }
}