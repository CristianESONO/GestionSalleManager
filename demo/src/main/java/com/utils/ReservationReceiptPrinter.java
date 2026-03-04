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
    private static final double RECEIPT_HEIGHT_POINTS = 72 * 18;
    private static final int LINE_HEIGHT = 8;
    private static final int SECTION_SPACING = 5;
    private static final double MARGIN_LEFT = 10;
    private static final double MARGIN_RIGHT = 10;
    private static final double CONTENT_WIDTH = RECEIPT_WIDTH_POINTS - MARGIN_LEFT - MARGIN_RIGHT;
    private static final String SEPARATOR_LINE = "════════════════════════════════════════";
    private static final String SEPARATOR_THIN = "────────────────────────────────────────";
    private static final java.awt.Color COLOR_HEADER = new java.awt.Color(26, 54, 93);
    private static final java.awt.Color COLOR_TOTAL = new java.awt.Color(45, 125, 70);
    private static final java.awt.Color COLOR_ALERT = new java.awt.Color(180, 50, 50);
    private Image logoImage;
    private Image socialMediaImage;
    private Image canalImage;
    private Image qrCodeImage;
    private String modePaiement;

    public ReservationReceiptPrinter(Reservation reservation, String userName, String modePaiement) {
        this(reservation, userName, false, 0, 0, modePaiement);
    }

    public ReservationReceiptPrinter(Reservation reservation, String userName, boolean isExtension,
                                    int additionalMinutes, double originalAmount, String modePaiement) {
        this.reservation = reservation;
        this.userName = userName;
        this.isExtension = isExtension;
        this.additionalMinutes = additionalMinutes;
        this.originalAmount = originalAmount;
        this.modePaiement = modePaiement;
        try {
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font fontNormal = new Font("SansSerif", Font.PLAIN, 7);
        Font fontBold = new Font("SansSerif", Font.BOLD, 8);
        Font fontHeader = new Font("SansSerif", Font.BOLD, 9);
        Font fontTicket = new Font("SansSerif", Font.BOLD, 8);
        Font fontItalic = new Font("SansSerif", Font.ITALIC, 6);
        Font fontHighlight = new Font("SansSerif", Font.BOLD, 8);
        int y = 0;

        // --- 1. LOGO (réduit) ---
        if (logoImage != null) {
            int logoWidth = (int) (CONTENT_WIDTH * 0.35);
            int logoHeight = (int) (logoImage.getHeight(null) * (double) logoWidth / logoImage.getWidth(null));
            if (logoHeight > 28) logoHeight = 28;
            int xLogo = (int) (MARGIN_LEFT + (CONTENT_WIDTH - logoWidth) / 2);
            g2d.drawImage(logoImage, xLogo, y, logoWidth, logoHeight, null);
            y += logoHeight + 3;
        }

        // --- 2. Infos entreprise (compact) ---
        g2d.setFont(fontHeader);
        g2d.setColor(COLOR_HEADER);
        centerString(g2d, "KAY PLAY GAMING ROOM", y);
        g2d.setColor(java.awt.Color.BLACK);
        y += LINE_HEIGHT;
        g2d.setFont(fontItalic);
        centerString(g2d, "Jaxaay, KEUR MASSAR · Tel. 33 813 47 20", y);
        y += SECTION_SPACING;

        // --- 3. Numéro de ticket et date ---
        centerString(g2d, SEPARATOR_LINE, y);
        y += LINE_HEIGHT;
        g2d.setFont(fontTicket);
        centerString(g2d, "RÉSERVATION N° " + reservation.getNumeroTicket(), y);
        y += LINE_HEIGHT;
        if (isExtension) {
            g2d.setFont(fontHighlight);
            g2d.setColor(COLOR_ALERT);
            centerString(g2d, "*** PROLONGATION DE SESSION ***", y);
            g2d.setColor(java.awt.Color.BLACK);
            y += LINE_HEIGHT;
        }
        g2d.setFont(fontNormal);
        String reservationDate = reservation.getReservationDate().format(
            DateTimeFormatter.ofPattern("EEE dd/MM/yyyy · HH:mm", Locale.FRENCH));
        centerString(g2d, reservationDate, y);
        y += LINE_HEIGHT;
        centerString(g2d, "Vendeur : " + userName, y);
        y += LINE_HEIGHT;
        centerString(g2d, SEPARATOR_LINE, y);
        y += SECTION_SPACING;

        // --- 4. Client ---
        g2d.setFont(fontBold);
        String clientName = (reservation.getClient() != null) ? reservation.getClient().getName() : "Non spécifié";
        if (clientName.length() > 28) clientName = clientName.substring(0, 25) + "...";
        centerString(g2d, "Client : " + clientName, y);
        y += 4;

        // --- 5. Tableau Poste / Durée / Total ---
        int colPosteWidth = 50;
        int colDureeWidth = 90;
        int colTotalWidth = 56;
        int tableWidth = colPosteWidth + colDureeWidth + colTotalWidth;
        int tableStartX = (int) (MARGIN_LEFT + (CONTENT_WIDTH - tableWidth) / 2);

        g2d.setFont(fontBold);
        g2d.drawString("Poste", tableStartX, y);
        g2d.drawString("Durée", tableStartX + colPosteWidth, y);
        g2d.drawString("Total CFA", tableStartX + colPosteWidth + colDureeWidth, y);
        y += LINE_HEIGHT;
        centerString(g2d, SEPARATOR_THIN, y);
        y += LINE_HEIGHT;

        g2d.setFont(fontNormal);
        String poste = (reservation.getPoste() != null) ? (reservation.getPoste().getName() != null ? reservation.getPoste().getName() : String.valueOf(reservation.getPoste().getId())) : "N/A";
        String duree;
        if (isExtension) {
            duree = String.format("%dh%02d (+%dmin)",
                reservation.getDuration().toHours(),
                reservation.getDuration().toMinutesPart(),
                additionalMinutes);
        } else {
            duree = String.format("%dh%02dmin",
                reservation.getDuration().toHours(),
                reservation.getDuration().toMinutesPart());
        }
        String total = String.format("%.0f", reservation.getTotalPrice());
        g2d.drawString(poste, tableStartX, y);
        g2d.drawString(duree, tableStartX + colPosteWidth, y);
        g2d.drawString(total, tableStartX + colPosteWidth + colDureeWidth, y);
        y += LINE_HEIGHT;

        if (isExtension) {
            g2d.setFont(fontItalic);
            String priceDetail = String.format("(dont %.0f CFA pour %d min suppl.)",
                reservation.getTotalPrice() - originalAmount, additionalMinutes);
            centerString(g2d, priceDetail, y);
            y += LINE_HEIGHT;
        }
        centerString(g2d, SEPARATOR_THIN, y);
        y += 4;

        // --- 6. Mode de paiement ---
        g2d.setFont(fontNormal);
        centerString(g2d, "Paiement : " + modePaiement, y);
        y += LINE_HEIGHT;

        // --- 7. Jeux (une ligne compacte) ---
        g2d.setFont(fontItalic);
        List<Game> gamesOnPoste = (reservation.getPoste() != null && reservation.getPoste().getGames() != null)
            ? reservation.getPoste().getGames() : new ArrayList<>();
        if (!gamesOnPoste.isEmpty()) {
            String jeux = gamesOnPoste.stream().filter(g -> g != null && g.getName() != null).map(Game::getName).limit(5).reduce((a, b) -> a + ", " + b).orElse("");
            if (jeux.length() > 42) jeux = jeux.substring(0, 39) + "...";
            centerString(g2d, "Jeux : " + jeux, y);
            y += LINE_HEIGHT;
        }

        // --- 8. Mentions légales (compact) ---
        centerString(g2d, SEPARATOR_THIN, y);
        y += LINE_HEIGHT;
        g2d.setFont(fontItalic);
        centerString(g2d, "Non remboursable · À conserver", y);

        return PAGE_EXISTS;
    }

    private void centerString(Graphics2D g2d, String text, int yPos) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (int) (MARGIN_LEFT + (CONTENT_WIDTH - textWidth) / 2);
        g2d.drawString(text, x, yPos);
    }

    public void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        paper.setSize(RECEIPT_WIDTH_POINTS, RECEIPT_HEIGHT_POINTS);
        paper.setImageableArea(0, 0, RECEIPT_WIDTH_POINTS, RECEIPT_HEIGHT_POINTS);
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
