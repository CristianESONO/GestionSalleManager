package com.core;

import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowManager {
    private static final Map<String, List<Stage>> windowsByView = new HashMap<>();

    public static void register(String viewName, Stage stage) {
        windowsByView.computeIfAbsent(viewName, k -> new ArrayList<>()).add(stage);
        stage.setOnCloseRequest(e -> windowsByView.get(viewName).remove(stage));
    }

    public static void closeWindowsForView(String viewName) {
        List<Stage> windows = windowsByView.get(viewName);
        if (windows != null) {
            for (Stage stage : new ArrayList<>(windows)) {
                if (stage.isShowing()) stage.close();
            }
            windows.clear();
        }
    }

    public static void closeAll() {
        for (String key : new ArrayList<>(windowsByView.keySet())) {
            closeWindowsForView(key);
        }
    }
}
