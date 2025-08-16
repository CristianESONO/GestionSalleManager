package com.utils;

import com.entities.Reservation;
import com.entities.Client;
import com.entities.Poste;
import com.entities.Game;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Image;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

public class ReservationReceiptPrinter implements Printable {

    private Reservation reservation;
    private String userName;
    private boolean isExtension;
    private int additionalMinutes;
    private double originalAmount;

    private static final double RECEIPT_WIDTH_POINTS = 220;
    private static final double INITIAL_RECEIPT_HEIGHT_POINTS = 72 * 18;
    private static final int LINE_HEIGHT = 10;
    private static final int SECTION_SPACING = 8;

    private Image logoImage;
    private Image socialMediaImage;
    private Image canalImage;
    private Image qrCodeImage;
    
     // Constructeur pour nouvelle réservation
    public ReservationReceiptPrinter(Reservation reservation, String userName) {
        this(reservation, userName, false, 0, 0);
    }

    public ReservationReceiptPrinter(Reservation reservation, String userName, boolean isExtension, int additionalMinutes, 
                                   double originalAmount) {
        this.reservation = reservation;
        this.userName = userName;
        this.isExtension = isExtension;
        this.additionalMinutes = additionalMinutes;
        this.originalAmount = originalAmount;

        try {
            // Chargement des images avec gestion d'erreur améliorée
            loadImages();
        } catch (Exception e) {
            System.err.println("Error loading images for receipt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadImages() throws Exception {
        InputStream logoStream = getClass().getResourceAsStream("/com/img/ticket.jpg");
        if (logoStream != null) {
            logoImage = ImageIO.read(logoStream);
        }

        InputStream rsStream = getClass().getResourceAsStream("/com/img/rs1.png");
        if (rsStream != null) {
            socialMediaImage = ImageIO.read(rsStream);
        }

        InputStream rsStream1 = getClass().getResourceAsStream("/com/img/rs.jpg");
        if (rsStream1 != null) {
            canalImage = ImageIO.read(rsStream1);
        }

        InputStream qrStream = getClass().getResourceAsStream("/com/img/QRCode.png");
        if (qrStream != null) {
            qrCodeImage = ImageIO.read(qrStream);
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Définition des polices
         Font fontNormal = new Font("Calibri", Font.PLAIN, 8);
        Font fontBold = new Font("Calibri", Font.BOLD, 9);
        Font fontHeader = new Font("Calibri", Font.BOLD, 10);
        Font fontItalic = new Font("Calibri", Font.ITALIC, 8);
        Font fontExtension = new Font("Calibri", Font.BOLD, 10);

        int y = 0;

        // --- 1. LOGO (Taille optimisée) ---
        if (logoImage != null) {
            int logoWidth = 140; // Conserve la qualité du logo
            int logoHeight = (int) (logoImage.getHeight(null) * (double) logoWidth / logoImage.getWidth(null));
            int xLogo = (int) ((RECEIPT_WIDTH_POINTS - logoWidth) / 2);
            g2d.drawImage(logoImage, xLogo, y, logoWidth, logoHeight, null);
            y += logoHeight + SECTION_SPACING;
        }

        // --- 2. Informations de l'entreprise ---
        g2d.setFont(fontNormal);
        centerString(g2d, "Jaxaay, Parcelle Unité 24, BP 17000, KEUR MASSAR", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "Tel. +221338220000", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "www.Kayplay.gamingroom.com", (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // --- 3. Numéro de ticket et date ---
        g2d.setFont(fontBold);
        centerString(g2d, reservation.getNumeroTicket(), (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;

        // Indication de prolongation
        if (isExtension) {
            g2d.setFont(fontExtension);
            g2d.setColor(java.awt.Color.RED);
            centerString(g2d, "*** PROLONGATION DE SESSION ***", (int) RECEIPT_WIDTH_POINTS, y);
            g2d.setColor(java.awt.Color.BLACK);
            y += LINE_HEIGHT;
        }

        // Date de réservation
        String reservationDate = reservation.getReservationDate().format(
            DateTimeFormatter.ofPattern("EEE dd/MM/yyyy HH:mm", Locale.FRENCH));
        centerString(g2d, reservationDate, (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // --- 4. Informations client ---
        g2d.setFont(fontBold);
        String clientName = (reservation.getClient() != null) ? reservation.getClient().getName() : "N/A";
        centerString(g2d, "CLIENT : " + clientName, (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // --- 5. Détails de la réservation (version compacte) ---
        g2d.setFont(fontBold);
        int leftMargin = 25;  // Augmenté de 15 à 25 (plus d'espace à gauche)
        int rightMargin = 25; // Augmenté de 15 à 25 (plus d'espace à droite)

        // En-têtes alignés
        g2d.setFont(fontBold);
        int col1 = leftMargin;
        int col2 = (int)(RECEIPT_WIDTH_POINTS * 0.5);
        int col3 = (int)RECEIPT_WIDTH_POINTS - rightMargin - g2d.getFontMetrics().stringWidth("Total");

        g2d.drawString("Poste", col1, y);
        g2d.drawString("Durée", col2 - g2d.getFontMetrics().stringWidth("Durée")/2, y);
        g2d.drawString("Total", col3, y);
        y += LINE_HEIGHT;


        // Ligne séparatrice
        centerString(g2d, "----------------------------", (int)RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;

        // Données
        g2d.setFont(fontNormal);
        String poste = (reservation.getPoste() != null) ? String.valueOf(reservation.getPoste().getId()) : "N/A";
        String duree;
        if (isExtension) {
            duree = String.format("%dh%02dmin (+%dmin)", 
                reservation.getDuration().toHours(),
                reservation.getDuration().toMinutesPart(),
                additionalMinutes);
        } else {
            duree = String.format("%dh%02dmin", 
                reservation.getDuration().toHours(),
                reservation.getDuration().toMinutesPart());
        }
        // Calcul du total
        String total = String.format("%.0f CFA", reservation.getTotalPrice());

        g2d.drawString(poste, col1, y);
        g2d.drawString(duree, col2 - g2d.getFontMetrics().stringWidth(duree)/2, y);
        g2d.drawString(total, col3, y);
        y += LINE_HEIGHT;

        // Détails de prix pour prolongation
        if (isExtension) {
            g2d.setFont(fontItalic);
            String priceDetail = String.format("(dont %.0f CFA pour %d minutes supplémentaires)", 
                                            reservation.getTotalPrice() - originalAmount, 
                                            additionalMinutes);
            centerString(g2d, priceDetail, (int)RECEIPT_WIDTH_POINTS, y);
            y += LINE_HEIGHT;
        }

        // Ligne séparatrice finale
        centerString(g2d, "----------------------------", (int)RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // --- 6. Liste des jeux ---
        g2d.setFont(fontBold);
        centerString(g2d, "VOUS POUVEZ JOUER A TOUS CES JEUX", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "PENDANT VOTRE SESSION !", (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        g2d.setFont(fontNormal);
        List<Game> gamesOnPoste = (reservation.getPoste() != null) ? 
            reservation.getPoste().getGames() : new ArrayList<>();
        
        if (gamesOnPoste.isEmpty()) {
            centerString(g2d, "Aucun jeu disponible", (int) RECEIPT_WIDTH_POINTS, y);
            y += LINE_HEIGHT;
        } else {
            for (Game game : gamesOnPoste) {
                if (game != null && game.getName() != null) {
                    centerString(g2d, game.getName(), (int) RECEIPT_WIDTH_POINTS, y);
                    y += LINE_HEIGHT;
                }
            }
        }
        y += SECTION_SPACING;

        // --- 7. Services supplémentaires ---
        g2d.setFont(fontBold);
        centerString(g2d, "PROFITEZ DE NOS ABONNEMENTS", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "YOUTUBE - NETFLIX - MYCANAL - WIFI", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "GRATUITEMENT !", (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // Image Canal
        if (canalImage != null) {
            int imgWidth = 100;
            int imgHeight = (int) (canalImage.getHeight(null) * (double) imgWidth / canalImage.getWidth(null));
            int xImg = (int) ((RECEIPT_WIDTH_POINTS - imgWidth) / 2);
            g2d.drawImage(canalImage, xImg, y, imgWidth, imgHeight, null);
            y += imgHeight + SECTION_SPACING;
        }

        // --- 8. Footer avec QR Code ---
        g2d.setFont(fontHeader);
        centerString(g2d, "KAY PLAY GAMING ROOM", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        g2d.setFont(fontNormal);
        centerString(g2d, "Votre passion pour les jeux vidéo prend vie !", (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // Réseaux sociaux
        centerString(g2d, "Suivez-nous sur", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        if (socialMediaImage != null) {
            int smWidth = 100;
            int smHeight = (int) (socialMediaImage.getHeight(null) * (double) smWidth / socialMediaImage.getWidth(null));
            int xSm = (int) ((RECEIPT_WIDTH_POINTS - smWidth) / 2);
            g2d.drawImage(socialMediaImage, xSm, y, smWidth, smHeight, null);
            y += smHeight + SECTION_SPACING;
        }

        // QR Code
        if (qrCodeImage != null) {
            int qrWidth = 70;
            int qrHeight = (int) (qrCodeImage.getHeight(null) * (double) qrWidth / qrCodeImage.getWidth(null));
            int xQr = (int) ((RECEIPT_WIDTH_POINTS - qrWidth) / 2);
            g2d.drawImage(qrCodeImage, xQr, y, qrWidth, qrHeight, null);
            y += qrHeight + SECTION_SPACING;
        }

        // Mentions légales
        g2d.setFont(fontItalic);
        centerString(g2d, "Ticket non remboursable", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "A conserver pour toute réclamation", (int) RECEIPT_WIDTH_POINTS, y);

        return PAGE_EXISTS;
    }

    private void centerString(Graphics2D g2d, String text, int pageWidth, int yPos) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (pageWidth - textWidth) / 2;
        g2d.drawString(text, x, yPos);
    }

    public void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);

        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        paper.setSize(RECEIPT_WIDTH_POINTS, INITIAL_RECEIPT_HEIGHT_POINTS);
        paper.setImageableArea(0, 0, RECEIPT_WIDTH_POINTS, INITIAL_RECEIPT_HEIGHT_POINTS);
        pf.setPaper(paper);

        job.setPrintable(this, pf);

        String printerName = "XP-80C"; 
        PrintService selectedPrintService = null;
        PrintService[] printServices = PrinterJob.lookupPrintServices();

        for (PrintService service : printServices) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                selectedPrintService = service;
                break;
            }
        }

        if (selectedPrintService != null) {
            try {
                job.setPrintService(selectedPrintService);
                job.print();
            } catch (PrinterException e) {
                System.err.println("Erreur d'impression: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                if (job.printDialog()) {
                    job.print();
                }
            } catch (PrinterException e) {
                System.err.println("Erreur lors de l'impression: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}