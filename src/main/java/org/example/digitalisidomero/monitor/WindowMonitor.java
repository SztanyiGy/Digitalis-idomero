package org.example.digitalisidomero.monitor;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;

public class WindowMonitor {

    private static final int MAX_TITLE_LENGTH = 1024;

    /**
     * Lekéri az aktív ablak címét (title)
     * @return Az aktív ablak címe, vagy "Unknown" ha nem sikerült
     */
    public String getActiveWindowTitle() {
        try {
            // Windows User32 API használata
            HWND hwnd = User32.INSTANCE.GetForegroundWindow();

            if (hwnd == null) {
                return "Unknown";
            }

            char[] windowText = new char[MAX_TITLE_LENGTH];
            User32.INSTANCE.GetWindowText(hwnd, windowText, MAX_TITLE_LENGTH);

            String title = Native.toString(windowText).trim();

            return title.isEmpty() ? "Unknown" : title;

        } catch (Exception e) {
            System.err.println("✗ Hiba az aktív ablak lekérdezésekor: " + e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Lekéri az aktív ablak folyamat nevét (exe fájl név)
     * @return A folyamat neve, pl. "chrome.exe"
     */
    public String getActiveWindowProcessName() {
        try {
            HWND hwnd = User32.INSTANCE.GetForegroundWindow();

            if (hwnd == null) {
                return "Unknown";
            }

            // Process ID lekérése
            IntByReference pid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);

            // Process név lekérése (egyszerűsített verzió)
            // Teljes implementációhoz szükség van a Kernel32-re is
            String processName = getProcessNameById(pid.getValue());

            return processName != null ? processName : "Unknown";

        } catch (Exception e) {
            System.err.println("✗ Hiba a folyamat név lekérdezésekor: " + e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Kombinálja az ablak címét és a folyamat nevét
     * @return Formátum: "folyamatnév - ablakcím"
     */
    public String getActiveWindowInfo() {
        String title = getActiveWindowTitle();
        String processName = getActiveWindowProcessName();

        if ("Unknown".equals(processName)) {
            return title;
        }

        return processName + " - " + title;
    }

    /**
     * Process név lekérése ID alapján
     * Ehhez további WinAPI hívások kellenének, most egyszerűsített
     */
    private String getProcessNameById(int pid) {
        try {
            // Windows PowerShell parancs futtatása a folyamat név lekéréséhez
            ProcessBuilder pb = new ProcessBuilder(
                    "powershell.exe",
                    "-Command",
                    "(Get-Process -Id " + pid + ").ProcessName"
            );

            Process process = pb.start();
            java.util.Scanner scanner = new java.util.Scanner(process.getInputStream());

            if (scanner.hasNextLine()) {
                String processName = scanner.nextLine().trim();
                scanner.close();
                return processName + ".exe";
            }

            scanner.close();

        } catch (Exception e) {
            // Nem kritikus hiba, csak nem tudjuk a pontos nevet
        }

        return null;
    }

    /**
     * Teszteléshez: kiírja az aktuális ablak infóit
     */
    public void printCurrentWindowInfo() {
        System.out.println("─────────────────────────────────");
        System.out.println("Aktív ablak címe: " + getActiveWindowTitle());
        System.out.println("Folyamat neve: " + getActiveWindowProcessName());
        System.out.println("Teljes info: " + getActiveWindowInfo());
        System.out.println("─────────────────────────────────");
    }
}