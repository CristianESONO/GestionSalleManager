package com.utils;

import com.core.AppConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.Locale;
import java.util.Optional;

/**
 * Affiche le QR (Wave / Orange Money) puis attend la confirmation du caissier
 * (« paiement reçu ») avant d'enregistrer la vente en base.
 */
public final class MobileMoneyQrConfirmDialog {

    private MobileMoneyQrConfirmDialog() {}

    /**
     * @param owner fenêtre parente
     * @param modeAffiche libellé ComboBox, ex. « Wave »
     * @param montantFcfa montant total
     * @return true si le caissier confirme la réception du paiement
     */
    public static boolean showAndWait(Window owner, String modeAffiche, double montantFcfa) {
        long amount = Math.round(montantFcfa);
        String methodApi = "Orange Money".equals(modeAffiche) ? "orange_money" : "wave";

        Optional<String> fromApi = PaymentQrIntentClient.fetchQrPayload(methodApi, amount);
        String staticPayload = "Orange Money".equals(modeAffiche)
                ? AppConfig.getPaymentQrOrangeStaticPayload()
                : AppConfig.getPaymentQrWaveStaticPayload();

        String qrData = fromApi.filter(s -> !s.isBlank())
                .or(() -> Optional.ofNullable(staticPayload).filter(s -> !s.isBlank()))
                .orElse(null);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(modeAffiche + " — paiement client");
        dialog.setHeaderText(String.format(Locale.FRANCE,
                "Montant : %,d FCFA — le client scanne le QR puis paie sur son téléphone.", amount));

        ButtonType confirmType = new ButtonType("Paiement reçu — valider la vente", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, cancelType);

        VBox content = new VBox(12);
        content.setPadding(new Insets(10));
        content.setAlignment(Pos.CENTER);

        Label hint = new Label();
        hint.setWrapText(true);
        hint.setMaxWidth(Region.USE_PREF_SIZE);

        ImageView qrView = new ImageView();
        qrView.setPreserveRatio(true);
        qrView.setFitWidth(260);
        qrView.setFitHeight(260);

        if (qrData != null) {
            try {
                qrView.setImage(QrCodeFxUtil.encodeQr(qrData, 280));
                hint.setText("Demandez au client de scanner ce code avec son application " + modeAffiche + ".");
            } catch (Exception e) {
                hint.setText("Impossible de générer le QR. Vérifiez la configuration ou le contenu renvoyé par l'API.\n"
                        + e.getMessage());
            }
        } else {
            hint.setText(
                    "Aucun QR configuré : renseignez « payment.qr.api.baseUrl » (script PHP sur votre domaine) "
                            + "ou « payment.qr." + ("Orange Money".equals(modeAffiche) ? "orange" : "wave")
                            + ".staticPayload » dans config.properties.\n\n"
                            + "Vous pouvez tout de même encaisser manuellement après vérification sur le téléphone du client.");
            qrView.setVisible(false);
            qrView.setManaged(false);
        }

        Label adminHint = new Label(
                "Après réception de l'argent sur votre compte marchand, cliquez sur « Paiement reçu ».");
        adminHint.setWrapText(true);
        adminHint.setStyle("-fx-font-weight: bold;");

        content.getChildren().addAll(hint, qrView, adminHint);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(420);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        DialogPane pane = dialog.getDialogPane();
        pane.setContent(scroll);

        dialog.setResultConverter(button -> button == confirmType);

        // Mettre le bouton vert en évidence
        dialog.setOnShown(ev -> {
            Button ok = (Button) pane.lookupButton(confirmType);
            if (ok != null) {
                ok.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
            }
        });

        Optional<Boolean> result = dialog.showAndWait();
        return Boolean.TRUE.equals(result.orElse(false));
    }
}
