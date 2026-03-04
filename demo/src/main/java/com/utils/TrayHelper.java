package com.utils;

import com.App;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Gestion de la zone de notification (systray) pour « minimiser » la fenêtre :
 * au clic sur Réduire, la fenêtre est masquée et une icône apparaît dans la barre des tâches ;
 * un clic sur l'icône (ou le menu « Restaurer ») réaffiche la fenêtre.
 * Utilise la réflexion pour éviter les problèmes de module java.desktop à la compilation.
 */
public final class TrayHelper {

    private static boolean trayAvailable = false;
    private static Stage mainStageRef;

    /**
     * Configure l'icône dans la zone de notification. À appeler une fois au démarrage (après stage.show()).
     * @return true si le tray a été configuré (minimiser = masquer vers le tray)
     */
    public static boolean setupTray(Stage stage) {
        if (stage == null) return false;
        try {
            Class<?> systemTrayClass = Class.forName("java.awt.SystemTray");
            Method isSupported = systemTrayClass.getMethod("isSupported");
            if (!Boolean.TRUE.equals(isSupported.invoke(null))) return false;
        } catch (Throwable t) {
            return false;
        }
        try {
            mainStageRef = stage;
            Class<?> systemTrayClass = Class.forName("java.awt.SystemTray");
            Method getDefaultSystemTray = systemTrayClass.getMethod("getDefaultSystemTray");
            Object tray = getDefaultSystemTray.invoke(null);
            if (tray == null) return false;

            Object image = loadTrayImage();
            Object popupMenu = buildPopupMenu();
            Class<?> trayIconClass = Class.forName("java.awt.TrayIcon");
            Class<?> imageClass = Class.forName("java.awt.Image");
            Object trayIcon = trayIconClass.getConstructor(imageClass, String.class, popupMenu.getClass())
                .newInstance(image, "GESTION KAYPLAY", popupMenu);
            trayIconClass.getMethod("setImageAutoSize", boolean.class).invoke(trayIcon, true);

            Class<?> actionListenerClass = Class.forName("java.awt.event.ActionListener");
            Object restoreListener = java.lang.reflect.Proxy.newProxyInstance(
                actionListenerClass.getClassLoader(),
                new Class<?>[] { actionListenerClass },
                (proxy, method, args) -> {
                    Platform.runLater(() -> {
                        Stage s = getStage();
                        if (s != null) { s.show(); s.toFront(); }
                    });
                    return null;
                });
            trayIconClass.getMethod("addActionListener", actionListenerClass).invoke(trayIcon, restoreListener);

            Method addMethod = tray.getClass().getMethod("add", trayIconClass);
            addMethod.invoke(tray, trayIcon);
            trayAvailable = true;
            return true;
        } catch (Throwable t) {
            System.err.println("TrayHelper: " + t.getMessage());
            return false;
        }
    }

    private static Object loadTrayImage() throws Exception {
        try (InputStream is = App.class.getResourceAsStream("/com/img/71xzcr0FFvL.jpg")) {
            if (is != null) {
                Class<?> imageIOClass = Class.forName("javax.imageio.ImageIO");
                Method read = imageIOClass.getMethod("read", InputStream.class);
                Object img = read.invoke(null, is);
                if (img != null) return img;
            }
        } catch (Throwable ignored) {}
        Class<?> toolkitClass = Class.forName("java.awt.Toolkit");
        Method getToolkit = toolkitClass.getMethod("getDefaultToolkit");
        Object toolkit = getToolkit.invoke(null);
        return toolkit.getClass().getMethod("createImage", byte[].class).invoke(toolkit, (Object) new byte[0]);
    }

    private static Object buildPopupMenu() throws Exception {
        Class<?> popupMenuClass = Class.forName("java.awt.PopupMenu");
        Object menu = popupMenuClass.getDeclaredConstructor().newInstance();
        Class<?> menuItemClass = Class.forName("java.awt.MenuItem");
        Object restoreItem = menuItemClass.getConstructor(String.class).newInstance("Restaurer");
        Object quitItem = menuItemClass.getConstructor(String.class).newInstance("Quitter");

        Class<?> actionListenerClass = Class.forName("java.awt.event.ActionListener");
        Object restoreListener = java.lang.reflect.Proxy.newProxyInstance(
            actionListenerClass.getClassLoader(),
            new Class<?>[] { actionListenerClass },
            (proxy, method, args) -> {
                Platform.runLater(() -> {
                    Stage s = getStage();
                    if (s != null) { s.show(); s.toFront(); }
                });
                return null;
            });
        Object quitListener = java.lang.reflect.Proxy.newProxyInstance(
            actionListenerClass.getClassLoader(),
            new Class<?>[] { actionListenerClass },
            (proxy, method, args) -> {
                Platform.runLater(() -> {
                    Stage s = getStage();
                    if (s != null) s.close();
                    Platform.exit();
                    System.exit(0);
                });
                return null;
            });

        restoreItem.getClass().getMethod("addActionListener", actionListenerClass).invoke(restoreItem, restoreListener);
        quitItem.getClass().getMethod("addActionListener", actionListenerClass).invoke(quitItem, quitListener);
        menu.getClass().getMethod("add", menuItemClass).invoke(menu, restoreItem);
        menu.getClass().getMethod("addSeparator").invoke(menu);
        menu.getClass().getMethod("add", menuItemClass).invoke(menu, quitItem);
        return menu;
    }

    private static Stage getStage() {
        if (mainStageRef != null) return mainStageRef;
        try {
            return App.getMainStage();
        } catch (Exception e) {
            return null;
        }
    }

    /** true si le tray a été configuré : le bouton Réduire masquera la fenêtre vers le tray. */
    public static boolean isTrayAvailable() {
        return trayAvailable;
    }
}
