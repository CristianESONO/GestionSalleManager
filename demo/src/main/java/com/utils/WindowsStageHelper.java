package com.utils;

import java.lang.reflect.Method;

/**
 * Active le bouton « minimiser » pour la fenêtre principale sous Windows.
 * Utilise JNA (via réflexion) pour trouver la fenêtre par titre, ajouter le style
 * WS_MINIMIZEBOX et minimiser via ShowWindow, sans accès à Glass (pas besoin de --add-exports).
 */
public final class WindowsStageHelper {

    private static final int WS_MINIMIZEBOX = 0x00020000;
    private static final int GWL_STYLE = -16;

    private static final String DEFAULT_TITLE = "GESTION KAYPLAY";

    /** HWND de la fenêtre principale (trouvée par titre), pour ShowWindow(SW_MINIMIZE). */
    private static Object mainWindowHwnd;
    /** Titre utilisé pour trouver la fenêtre (pour éviter de re-chercher si le titre n'a pas changé). */
    private static String lastUsedTitle;

    /**
     * Trouve la fenêtre par son titre, ajoute WS_MINIMIZEBOX à son style.
     * À appeler après stage.show() (ex. dans Platform.runLater).
     * @param windowTitle titre de la fenêtre (stage.getTitle()), ou null pour utiliser "GESTION KAYPLAY"
     */
    public static void enableMinimizeForMainStage(String windowTitle) {
        if (!isWindows()) return;
        String title = (windowTitle != null && !windowTitle.isEmpty()) ? windowTitle : DEFAULT_TITLE;
        try {
            Object user32 = Class.forName("net.java.dev.jna.platform.win32.User32").getField("INSTANCE").get(null);
            Method findWindow = user32.getClass().getMethod("FindWindow", String.class, String.class);
            Object hwnd = findWindow.invoke(user32, null, title);
            if (hwnd == null) return;

            Method getWindowLong = user32.getClass().getMethod("GetWindowLong", Class.forName("net.java.dev.jna.platform.win32.WinDef$HWND"), int.class);
            Method setWindowLong = user32.getClass().getMethod("SetWindowLong", Class.forName("net.java.dev.jna.platform.win32.WinDef$HWND"), int.class, int.class);
            int oldStyle = ((Number) getWindowLong.invoke(user32, hwnd, GWL_STYLE)).intValue();
            setWindowLong.invoke(user32, hwnd, GWL_STYLE, oldStyle | WS_MINIMIZEBOX);

            mainWindowHwnd = hwnd;
            lastUsedTitle = title;
        } catch (Throwable t) {
            System.err.println("WindowsStageHelper: enableMinimize: " + t.getMessage());
        }
    }

    /**
     * Minimise la fenêtre principale (celle trouvée par titre).
     * À appeler au clic sur le bouton « minimiser ».
     * @param windowTitle titre actuel de la fenêtre (stage.getTitle()), ou null pour "GESTION KAYPLAY"
     * @return true si la minimisation a été faite via JNA (Windows), false sinon (utiliser setIconified en fallback)
     */
    public static boolean minimizeMainWindow(String windowTitle) {
        if (!isWindows()) return false;
        String title = (windowTitle != null && !windowTitle.isEmpty()) ? windowTitle : DEFAULT_TITLE;
        try {
            Object hwnd = mainWindowHwnd;
            if (hwnd == null || !title.equals(lastUsedTitle)) {
                Object user32 = Class.forName("net.java.dev.jna.platform.win32.User32").getField("INSTANCE").get(null);
                Method findWindow = user32.getClass().getMethod("FindWindow", String.class, String.class);
                hwnd = findWindow.invoke(user32, null, title);
                if (hwnd == null) return false;
                mainWindowHwnd = hwnd;
                lastUsedTitle = title;
            }
            Object user32 = Class.forName("net.java.dev.jna.platform.win32.User32").getField("INSTANCE").get(null);
            int swMinimize = Class.forName("net.java.dev.jna.platform.win32.WinUser").getField("SW_MINIMIZE").getInt(null);
            Method showWindow = user32.getClass().getMethod("ShowWindow", Class.forName("net.java.dev.jna.platform.win32.WinDef$HWND"), int.class);
            showWindow.invoke(user32, hwnd, swMinimize);
            return true;
        } catch (Throwable t) {
            System.err.println("WindowsStageHelper: minimize: " + t.getMessage());
            return false;
        }
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }
}
