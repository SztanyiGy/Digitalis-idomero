package org.example.digitalisidomero.database.dao;

import org.example.digitalisidomero.database.DatabaseConnection;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    // Alkalmazás mentése (INSERT)
    public int save(Application app) {
        String sql = "INSERT INTO applications (name, display_name, category) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, app.getName());
            pstmt.setString(2, app.getDisplayName());
            pstmt.setString(3, app.getCategory().name());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // SQLite-nál last_insert_rowid() használata
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        app.setId(id);
                        System.out.println("✓ Alkalmazás mentve: " + app.getDisplayName() + " (ID: " + id + ")");
                        return id;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Alkalmazás mentési hiba: " + app.getName());
            e.printStackTrace();
        }
        return -1;
    }

    // Alkalmazás keresése név alapján
    public Application findByName(String name) {
        String sql = "SELECT * FROM applications WHERE name = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractApplicationFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Alkalmazás keresési hiba: " + name);
            e.printStackTrace();
        }
        return null;
    }

    // Alkalmazás keresése ID alapján
    public Application findById(int id) {
        String sql = "SELECT * FROM applications WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractApplicationFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Alkalmazás ID keresési hiba: " + id);
            e.printStackTrace();
        }
        return null;
    }

    // Összes alkalmazás lekérése
    public List<Application> findAll() {
        String sql = "SELECT * FROM applications ORDER BY display_name";
        List<Application> applications = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                applications.add(extractApplicationFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Alkalmazások lekérési hiba!");
            e.printStackTrace();
        }
        return applications;
    }

    // Alkalmazás frissítése (UPDATE)
    public boolean update(Application app) {
        String sql = "UPDATE applications SET name = ?, display_name = ?, category = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, app.getName());
            pstmt.setString(2, app.getDisplayName());
            pstmt.setString(3, app.getCategory().name());
            pstmt.setInt(4, app.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✓ Alkalmazás frissítve: " + app.getDisplayName());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("✗ Alkalmazás frissítési hiba: " + app.getName());
            e.printStackTrace();
        }
        return false;
    }

    // Alkalmazás törlése
    public boolean delete(int id) {
        String sql = "DELETE FROM applications WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✓ Alkalmazás törölve (ID: " + id + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("✗ Alkalmazás törlési hiba (ID: " + id + ")");
            e.printStackTrace();
        }
        return false;
    }

    // Alkalmazás keresése vagy létrehozása (ha nem létezik)
    public Application findOrCreate(String name, String displayName, Category category) {
        Application app = findByName(name);
        if (app == null) {
            app = new Application(name, displayName, category);
            save(app);
        }
        return app;
    }

    // Segédfüggvény: Application objektum létrehozása ResultSet-ből
    private Application extractApplicationFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String displayName = rs.getString("display_name");
        Category category = Category.valueOf(rs.getString("category"));

        return new Application(id, name, displayName, category);
    }

    public Application findByProcessName(String processName) {
        return null;
    }
}