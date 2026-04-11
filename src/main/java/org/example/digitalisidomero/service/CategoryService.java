package org.example.digitalisidomero.service;

import org.example.digitalisidomero.database.dao.ApplicationDAO;
import org.example.digitalisidomero.model.Application;
import org.example.digitalisidomero.model.Category;

import java.util.HashMap;
import java.util.Map;

public class CategoryService {

    private final ApplicationDAO applicationDAO;

    // Alapértelmezett kategóriák (gyakori alkalmazásokhoz)
    private static final Map<String, Category> DEFAULT_CATEGORIES = new HashMap<>();

    static {
        // Fejlesztés / Munka
        DEFAULT_CATEGORIES.put("idea64.exe", Category.WORK);
        DEFAULT_CATEGORIES.put("code.exe", Category.WORK);
        DEFAULT_CATEGORIES.put("devenv.exe", Category.WORK);
        DEFAULT_CATEGORIES.put("eclipse.exe", Category.WORK);
        DEFAULT_CATEGORIES.put("pycharm64.exe", Category.WORK);

        // Office / Munka
        DEFAULT_CATEGORIES.put("EXCEL.EXE", Category.WORK);
        DEFAULT_CATEGORIES.put("WINWORD.EXE", Category.WORK);
        DEFAULT_CATEGORIES.put("POWERPNT.EXE", Category.WORK);
        DEFAULT_CATEGORIES.put("OUTLOOK.EXE", Category.WORK);

        // Böngészők - alapértelmezetten tanulás/munka
        DEFAULT_CATEGORIES.put("chrome.exe", Category.STUDY);
        DEFAULT_CATEGORIES.put("firefox.exe", Category.STUDY);
        DEFAULT_CATEGORIES.put("msedge.exe", Category.STUDY);
        DEFAULT_CATEGORIES.put("opera.exe", Category.STUDY);


        // Szórakozás
        DEFAULT_CATEGORIES.put("spotify.exe", Category.ENTERTAINMENT);
        DEFAULT_CATEGORIES.put("vlc.exe", Category.ENTERTAINMENT);
        DEFAULT_CATEGORIES.put("steam.exe", Category.ENTERTAINMENT);
        DEFAULT_CATEGORIES.put("EpicGamesLauncher.exe", Category.ENTERTAINMENT);

        // Játékok (gyakori játékok)
        DEFAULT_CATEGORIES.put("LeagueClient.exe", Category.ENTERTAINMENT);
        DEFAULT_CATEGORIES.put("Minecraft.exe", Category.ENTERTAINMENT);
        DEFAULT_CATEGORIES.put("csgo.exe", Category.ENTERTAINMENT);
        DEFAULT_CATEGORIES.put("RobloxPlayerBeta.exe", Category.ENTERTAINMENT);
    }

    public CategoryService() {
        this.applicationDAO = new ApplicationDAO();
    }

    /**
     * Alapértelmezett kategória lekérése egy alkalmazáshoz
     * @param processName Folyamat neve (pl. "chrome.exe")
     * @return Alapértelmezett kategória vagy OTHER
     */
    public Category getDefaultCategory(String processName) {
        return DEFAULT_CATEGORIES.getOrDefault(processName, Category.OTHER);
    }

    /**
     * Ellenőrzi, hogy van-e alapértelmezett kategória
     * @param processName Folyamat neve
     * @return true ha van alapértelmezett, false ha nincs
     */
    public boolean hasDefaultCategory(String processName) {
        return DEFAULT_CATEGORIES.containsKey(processName);
    }

    /**
     * Alkalmazás kategóriájának megváltoztatása
     * @param applicationId Alkalmazás ID
     * @param newCategory Új kategória
     * @return true ha sikeres, false ha nem
     */
    public boolean updateApplicationCategory(int applicationId, Category newCategory) {
        Application app = applicationDAO.findById(applicationId);
        if (app != null) {
            app.setCategory(newCategory);
            return applicationDAO.update(app);
        }
        return false;
    }

    /**
     * Alkalmazás kategóriájának megváltoztatása név alapján
     * @param processName Folyamat neve
     * @param newCategory Új kategória
     * @return true ha sikeres, false ha nem
     */
    public boolean updateApplicationCategoryByName(String processName, Category newCategory) {
        Application app = applicationDAO.findByName(processName);
        if (app != null) {
            app.setCategory(newCategory);
            return applicationDAO.update(app);
        }
        return false;
    }

    /**
     * Alkalmazás kategóriájának lekérése
     * @param applicationId Alkalmazás ID
     * @return Kategória vagy null ha nem található
     */
    public Category getApplicationCategory(int applicationId) {
        Application app = applicationDAO.findById(applicationId);
        return app != null ? app.getCategory() : null;
    }

    /**
     * Alkalmazás kategóriájának lekérése név alapján
     * @param processName Folyamat neve
     * @return Kategória vagy null ha nem található
     */
    public Category getApplicationCategoryByName(String processName) {
        Application app = applicationDAO.findByName(processName);
        return app != null ? app.getCategory() : null;
    }

    /**
     * Összes kategória lekérése (az enum-ból)
     * @return Category tömb
     */
    public Category[] getAllCategories() {
        return Category.values();
    }

    /**
     * Kategória szín kódja (UI-hoz)
     * @param category Kategória
     * @return Hex szín kód
     */
    public static String getCategoryColor(Category category) {
        return switch (category) {
            case WORK -> "#3498db";        // Kék
            case STUDY -> "#2ecc71";       // Zöld
            case ENTERTAINMENT -> "#e74c3c"; // Piros
            case OTHER -> "#95a5a6";       // Szürke
        };
    }

    /**
     * Kategória ikon (emoji) UI-hoz
     * @param category Kategória
     * @return Emoji string
     */
    public static String getCategoryIcon(Category category) {
        return switch (category) {
            case WORK -> "💼";
            case STUDY -> "📚";
            case ENTERTAINMENT -> "🎮";
            case OTHER -> "📱";
        };
    }

    /**
     * Új alapértelmezett kategória hozzáadása futásidőben
     * @param processName Folyamat neve
     * @param category Kategória
     */
    public void addDefaultCategory(String processName, Category category) {
        DEFAULT_CATEGORIES.put(processName, category);
    }

    /**
     * Alapértelmezett kategóriák számának lekérése
     * @return Hány alapértelmezett kategória van
     */
    public int getDefaultCategoriesCount() {
        return DEFAULT_CATEGORIES.size();
    }
}