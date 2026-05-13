package com.utils;

import java.util.Optional;

/** Extraction minimale d'un champ chaîne JSON (sans dépendance Jackson). */
final class JsonSmall {

    private JsonSmall() {}

    static Optional<String> extractStringField(String json, String field) {
        if (json == null || field == null) {
            return Optional.empty();
        }
        String needle = "\"" + field + "\"";
        int i = json.indexOf(needle);
        if (i < 0) {
            return Optional.empty();
        }
        int colon = json.indexOf(':', i + needle.length());
        if (colon < 0) {
            return Optional.empty();
        }
        int quote = json.indexOf('"', colon + 1);
        if (quote < 0) {
            return Optional.empty();
        }
        StringBuilder out = new StringBuilder();
        for (int j = quote + 1; j < json.length(); j++) {
            char ch = json.charAt(j);
            if (ch == '"') {
                break;
            }
            if (ch == '\\' && j + 1 < json.length()) {
                j++;
                out.append(json.charAt(j));
            } else {
                out.append(ch);
            }
        }
        return Optional.of(out.toString());
    }
}
