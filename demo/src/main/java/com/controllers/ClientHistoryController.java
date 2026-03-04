package com.controllers;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.GameSession;
import com.entities.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Fenêtre Historique client : sessions de jeu + infos client, bouton Générer PDF (paysage).
 */
public class ClientHistoryController {

    @FXML private Label clientInfoLabel;
    @FXML private TableView<GameSession> historyTable;
    @FXML private TableColumn<GameSession, String> colDateResa, colTicket, colPoste, colJeu, colDuree, colMontant, colPoints, colStatut;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Client client;
    private List<GameSession> sessions;

    public void setClient(Client client) {
        this.client = client;
        if (client == null) return;
        sessions = Fabrique.getService().findGameSessionsByClientIdWithRelations(client.getId());
        if (sessions == null) sessions = List.of();

        String nom = client.getName() != null ? client.getName() : "";
        String tel = client.getPhone() != null ? client.getPhone() : "";
        String dateCreation = client.getRegistrationDate() != null
            ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(client.getRegistrationDate()) : "-";
        long dureeTotaleMin = sessions.stream()
            .mapToLong(s -> s.getPaidDuration() != null ? s.getPaidDuration().toMinutes() : 0)
            .sum();
        double montantTotal = sessions.stream()
            .mapToDouble(GameSession::calculateSessionPrice)
            .sum();
        int pointsTotal = client.getLoyaltyPoints();

        clientInfoLabel.setText(String.format(
            "Client : %s  |  Tél : %s  |  Création compte : %s  |  Durée totale jeu : %dh%02dmin  |  Montant total : %.0f F  |  Points fidélité : %d",
            nom, tel, dateCreation, dureeTotaleMin / 60, dureeTotaleMin % 60, montantTotal, pointsTotal));

        colDateResa.setCellValueFactory(cell -> {
            GameSession s = cell.getValue();
            Reservation r = s != null ? s.getReservation() : null;
            if (r != null && r.getReservationDate() != null)
                return new javafx.beans.property.SimpleStringProperty(r.getReservationDate().format(DATE_FORMAT));
            if (s != null && s.getStartTime() != null)
                return new javafx.beans.property.SimpleStringProperty(s.getStartTime().format(DATE_FORMAT));
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colTicket.setCellValueFactory(cell -> {
            Reservation r = cell.getValue() != null ? cell.getValue().getReservation() : null;
            return new javafx.beans.property.SimpleStringProperty(r != null && r.getNumeroTicket() != null ? r.getNumeroTicket() : "-");
        });
        colPoste.setCellValueFactory(cell -> {
            var p = cell.getValue() != null ? cell.getValue().getPoste() : null;
            return new javafx.beans.property.SimpleStringProperty(p != null && p.getName() != null ? p.getName() : (p != null ? "Poste " + p.getId() : "-"));
        });
        colJeu.setCellValueFactory(cell -> {
            var g = cell.getValue() != null ? cell.getValue().getGame() : null;
            return new javafx.beans.property.SimpleStringProperty(g != null && g.getName() != null ? g.getName() : "-");
        });
        colDuree.setCellValueFactory(cell -> {
            Duration d = cell.getValue() != null ? cell.getValue().getPaidDuration() : null;
            if (d == null) return new javafx.beans.property.SimpleStringProperty("-");
            return new javafx.beans.property.SimpleStringProperty(String.format("%dh%02d", d.toHours(), d.toMinutesPart()));
        });
        colMontant.setCellValueFactory(cell -> {
            GameSession s = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(s != null ? String.format("%.0f", s.calculateSessionPrice()) : "-");
        });
        colPoints.setCellValueFactory(cell -> {
            Duration d = cell.getValue() != null ? cell.getValue().getPaidDuration() : null;
            int pts = (d != null && !d.isZero()) ? (int) (d.toMinutes() / 15) : 0;
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(pts));
        });
        colStatut.setCellValueFactory(cell -> {
            String st = cell.getValue() != null ? cell.getValue().getStatus() : null;
            return new javafx.beans.property.SimpleStringProperty(st != null ? st : "-");
        });

        historyTable.getItems().setAll(sessions);
    }

    @FXML
    private void genererPdf() {
        if (client == null || sessions == null) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer l'historique client");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        String safeName = (client.getName() != null ? client.getName().replaceAll("[^a-zA-Z0-9]", "_") : "client") + "_historique.pdf";
        fc.setInitialFileName(safeName);
        File file = fc.showSaveDialog(clientInfoLabel.getScene().getWindow());
        if (file == null) return;
        try {
            genererPdfLandscape(client, sessions, file);
            ControllerUtils.showInfoAlert("Succès", "PDF enregistré : " + file.getAbsolutePath());
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur PDF", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    /** Appelable depuis UserController pour générer le PDF sans ouvrir la fenêtre. */
    public static void genererPdfLandscape(Client client, List<GameSession> sessions, File file) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(842, 595)); // A4 paysage (PDFBox 3)
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 40;
            float w = page.getMediaBox().getWidth();
            float h = page.getMediaBox().getHeight();
            float y = h - margin;

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText("Historique des sessions — " + (client.getName() != null ? client.getName() : "Client"));
            cs.endText();
            y -= 20;

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            String infos = String.format("Tél : %s  |  Date création : %s  |  Durée totale : %d min  |  Montant total : %.0f F  |  Points fidélité : %d",
                client.getPhone() != null ? client.getPhone() : "-",
                client.getRegistrationDate() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(client.getRegistrationDate()) : "-",
                sessions.stream().mapToLong(s -> s.getPaidDuration() != null ? s.getPaidDuration().toMinutes() : 0).sum(),
                sessions.stream().mapToDouble(GameSession::calculateSessionPrice).sum(),
                client.getLoyaltyPoints());
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText(infos.length() > 120 ? infos.substring(0, 117) + "..." : infos);
            cs.endText();
            y -= 25;

            String[] headers = { "Date résa", "N° Ticket", "Poste", "Jeu", "Durée", "Montant", "Pts", "Statut" };
            float colW = (w - 2 * margin) / headers.length;
            float rowH = 18;
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
            cs.setNonStrokingColor(0.2f, 0.35f, 0.55f);
            cs.addRect(margin, y - rowH, w - 2 * margin, rowH);
            cs.fill();
            cs.setNonStrokingColor(1, 1, 1);
            float x = margin;
            for (String hdr : headers) {
                cs.beginText();
                cs.newLineAtOffset(x + 3, y - rowH + 5);
                cs.showText(hdr.length() > 10 ? hdr.substring(0, 9) : hdr);
                cs.endText();
                x += colW;
            }
            cs.setNonStrokingColor(0, 0, 0);
            y -= rowH;

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
            for (GameSession s : sessions) {
                if (y < margin + rowH) break;
                String dateStr = "-";
                if (s.getReservation() != null && s.getReservation().getReservationDate() != null)
                    dateStr = s.getReservation().getReservationDate().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"));
                else if (s.getStartTime() != null) dateStr = s.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"));
                String ticket = s.getReservation() != null && s.getReservation().getNumeroTicket() != null ? s.getReservation().getNumeroTicket() : "-";
                String poste = s.getPoste() != null ? (s.getPoste().getName() != null ? s.getPoste().getName() : "P" + s.getPoste().getId()) : "-";
                String jeu = s.getGame() != null && s.getGame().getName() != null ? s.getGame().getName() : "-";
                String duree = s.getPaidDuration() != null ? String.format("%dh%02d", s.getPaidDuration().toHours(), s.getPaidDuration().toMinutesPart()) : "-";
                String montant = String.format("%.0f", s.calculateSessionPrice());
                int pts = (s.getPaidDuration() != null && !s.getPaidDuration().isZero()) ? (int)(s.getPaidDuration().toMinutes() / 15) : 0;
                String statut = s.getStatus() != null ? s.getStatus() : "-";

                String[] row = { dateStr, ticket, poste, jeu, duree, montant, String.valueOf(pts), statut };
                x = margin;
                for (String cell : row) {
                    cs.beginText();
                    cs.newLineAtOffset(x + 2, y - 12);
                    cs.showText(cell.length() > 14 ? cell.substring(0, 13) : cell);
                    cs.endText();
                    x += colW;
                }
                y -= rowH;
            }

            cs.close();
            doc.save(file);
        }
    }

    @FXML
    private void fermer() {
        ((Stage) clientInfoLabel.getScene().getWindow()).close();
    }
}
