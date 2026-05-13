package com.controllers;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.Reservation;
import com.utils.ReservationDisplayUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Fenêtre Historique client : réservations du client + totaux (durée, montant, points) comme dans la réservation.
 */
public class ClientHistoryController {

    @FXML private Label clientInfoLabel;
    @FXML private TableView<Reservation> historyTable;
    @FXML private TableColumn<Reservation, String> colDateResa, colTicket, colPoste, colJeu, colDuree, colMontant, colPoints, colStatut;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Client client;
    private List<Reservation> reservations;

    public void setClient(Client client) {
        this.client = client;
        if (client == null) return;
        reservations = Fabrique.getService().findReservationsByClientIdWithRelations(client.getId());
        if (reservations == null) reservations = List.of();

        String nom = client.getName() != null ? client.getName() : "";
        String tel = client.getPhone() != null ? client.getPhone() : "";
        String dateCreation = client.getRegistrationDate() != null
            ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(client.getRegistrationDate()) : "-";
        long dureeTotaleMin = reservations.stream()
            .mapToLong(r -> r.getDuration() != null ? r.getDuration().toMinutes() : 0)
            .sum();
        double montantTotal = reservations.stream()
            .mapToDouble(Reservation::getTotalPrice)
            .sum();
        int pointsTotal = client.getLoyaltyPoints();

        clientInfoLabel.setText(String.format(
            "Client : %s  |  Tél : %s  |  Création compte : %s  |  Durée totale : %dh%02dmin  |  Montant total : %.0f F  |  Points fidélité : %d",
            nom, tel, dateCreation, dureeTotaleMin / 60, dureeTotaleMin % 60, montantTotal, pointsTotal));

        colDateResa.setCellValueFactory(cell -> {
            Reservation r = cell.getValue();
            if (r != null && r.getReservationDate() != null)
                return new javafx.beans.property.SimpleStringProperty(r.getReservationDate().format(DATE_FORMAT));
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colTicket.setCellValueFactory(cell -> {
            Reservation r = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(r != null && r.getNumeroTicket() != null ? r.getNumeroTicket() : "-");
        });
        colPoste.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(ReservationDisplayUtils.safeGetPosteName(cell.getValue())));
        colJeu.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(ReservationDisplayUtils.safeGetGameName(cell.getValue())));
        colDuree.setCellValueFactory(cell -> {
            Duration d = cell.getValue() != null ? cell.getValue().getDuration() : null;
            if (d == null) return new javafx.beans.property.SimpleStringProperty("-");
            return new javafx.beans.property.SimpleStringProperty(String.format("%dh%02d", d.toHours(), d.toMinutesPart()));
        });
        colMontant.setCellValueFactory(cell -> {
            Reservation r = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(r != null ? String.format("%.0f", r.getTotalPrice()) : "-");
        });
        colPoints.setCellValueFactory(cell -> {
            Duration d = cell.getValue() != null ? cell.getValue().getDuration() : null;
            int pts = (d != null && !d.isZero()) ? (int) (d.toMinutes() / 15) : 0;
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(pts));
        });
        colStatut.setCellValueFactory(cell -> {
            String st = cell.getValue() != null ? cell.getValue().getStatus() : null;
            return new javafx.beans.property.SimpleStringProperty(st != null ? st : "-");
        });

        historyTable.getItems().setAll(reservations);
    }

    @FXML
    private void genererPdf() {
        if (client == null || reservations == null) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer l'historique client");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        String safeName = (client.getName() != null ? client.getName().replaceAll("[^a-zA-Z0-9]", "_") : "client") + "_historique.pdf";
        fc.setInitialFileName(safeName);
        File file = fc.showSaveDialog(clientInfoLabel.getScene().getWindow());
        if (file == null) return;
        try {
            genererPdfLandscape(client, reservations, file);
            ControllerUtils.showInfoAlert("Succès", "PDF enregistré : " + file.getAbsolutePath());
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur PDF", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    /** Appelable depuis UserController pour générer le PDF sans ouvrir la fenêtre. */
    public static void genererPdfLandscape(Client client, List<Reservation> reservations, File file) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(842, 595)); // A4 paysage
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 40;
            float w = page.getMediaBox().getWidth();
            float h = page.getMediaBox().getHeight();
            float y = h - margin;

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText("Historique des réservations — " + (client.getName() != null ? client.getName() : "Client"));
            cs.endText();
            y -= 20;

            long dureeTotaleMin = reservations.stream().mapToLong(r -> r.getDuration() != null ? r.getDuration().toMinutes() : 0).sum();
            double montantTotal = reservations.stream().mapToDouble(Reservation::getTotalPrice).sum();
            int pointsFidelite = client.getLoyaltyPoints();
            String dureeHMin = String.format("%dh%02dmin", dureeTotaleMin / 60, dureeTotaleMin % 60);

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            // Ligne 1 : Tél et date création
            String infos1 = String.format("Tél : %s  |  Date création : %s",
                client.getPhone() != null ? client.getPhone() : "-",
                client.getRegistrationDate() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(client.getRegistrationDate()) : "-");
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText(infos1.length() > 100 ? infos1.substring(0, 97) + "..." : infos1);
            cs.endText();
            y -= 14;
            // Ligne 2 : Durée totale (en heure minute) et montant
            String infos2 = String.format("Durée totale : %s  |  Montant total : %.0f F", dureeHMin, montantTotal);
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText(infos2);
            cs.endText();
            y -= 14;
            // Ligne 3 : Points fidélité (ligne dédiée pour ne pas couper)
            String infos3 = "Points fidélité : " + pointsFidelite;
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText(infos3);
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
            for (Reservation r : reservations) {
                if (y < margin + rowH) break;
                String dateStr = r.getReservationDate() != null ? r.getReservationDate().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")) : "-";
                String ticket = r.getNumeroTicket() != null ? r.getNumeroTicket() : "-";
                String poste = ReservationDisplayUtils.safeGetPosteName(r);
                String jeu = ReservationDisplayUtils.safeGetGameName(r);
                String duree = r.getDuration() != null ? String.format("%dh%02d", r.getDuration().toHours(), r.getDuration().toMinutesPart()) : "-";
                String montant = String.format("%.0f", r.getTotalPrice());
                int pts = (r.getDuration() != null && !r.getDuration().isZero()) ? (int)(r.getDuration().toMinutes() / 15) : 0;
                String statut = r.getStatus() != null ? r.getStatus() : "-";

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
