package com.utils;

import com.core.AppConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * Appelle un endpoint HTTPS minimal sur votre domaine (PHP) pour obtenir le texte à encoder en QR.
 * Les clés Wave / Orange restent côté serveur ; l'appli bureau n'est pas un site web pour le client.
 */
public final class PaymentQrIntentClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private PaymentQrIntentClient() {}

    /**
     * @param methodApi "wave" ou "orange_money"
     * @param amountFcfa montant arrondi en francs CFA
     */
    public static Optional<String> fetchQrPayload(String methodApi, long amountFcfa) {
        String base = AppConfig.getPaymentQrApiBaseUrl();
        if (base == null || base.isBlank()) {
            return Optional.empty();
        }
        String trimmed = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String path = AppConfig.getPaymentQrApiPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String url = trimmed + path;

        String body = String.format(Locale.ROOT,
                "{\"method\":\"%s\",\"amount\":%d}",
                escapeJson(methodApi),
                amountFcfa);

        try {
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            String key = AppConfig.getPaymentQrApiKey();
            if (key != null && !key.isBlank()) {
                b.header("X-Desktop-Key", key.trim());
            }
            HttpResponse<String> resp = HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                return Optional.empty();
            }
            return JsonSmall.extractStringField(resp.body(), "qrPayload");
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
