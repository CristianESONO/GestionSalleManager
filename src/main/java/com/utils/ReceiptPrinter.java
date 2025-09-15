package com.utils;

import com.entities.Produit;
import java.awt.*;
import java.awt.print.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.imageio.ImageIO;
import java.io.InputStream;
import javax.print.PrintService;

public class ReceiptPrinter implements Printable {
    private Map<Produit, Integer> produitsDansLePanier;
    private double montantTotal;
    private String numeroTicket;
    private String userName;
    private static final double RECEIPT_WIDTH_POINTS = 220;
    private static final double RECEIPT_HEIGHT_POINTS = 72 * 20; // Ajusté pour le contenu
    private static final int LINE_HEIGHT = 10;
    private static final int SECTION_SPACING = 6;
    private Image logoImage;
    private Image socialMediaImage;
    private Image canalImage;
    private Image qrCodeImage;
    private String modePaiement;

    public ReceiptPrinter(Map<Produit, Integer> produitsDansLePanier, double montantTotal, String numeroTicket, String userName, String modePaiement) {
        this.produitsDansLePanier = produitsDansLePanier;
        this.montantTotal = montantTotal;
        this.numeroTicket = numeroTicket;
        this.userName = userName;
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

        // Définition des polices
        Font fontNormal = new Font("Calibri", Font.PLAIN, 8);
        Font fontBold = new Font("Calibri", Font.BOLD, 9);
        Font fontHeader = new Font("Calibri", Font.BOLD, 10);
        Font fontItalic = new Font("Calibri", Font.ITALIC, 8);

        int y = 0;

        // --- 1. LOGO ---
        if (logoImage != null) {
            int logoWidth = 100;
            int logoHeight = (int) (logoImage.getHeight(null) * (double) logoWidth / logoImage.getWidth(null));
            int xLogo = (int) ((RECEIPT_WIDTH_POINTS - logoWidth) / 2);
            g2d.drawImage(logoImage, xLogo, y, logoWidth, logoHeight, null);
            y += logoHeight + SECTION_SPACING;
        }

        // --- 2. Informations de l'entreprise ---
        g2d.setFont(fontNormal);
        centerString(g2d, "Jaxaay, Parcelle Unité 24, BP 17000, KEUR MASSAR", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "Tel. +221 33 813 47 20 / 77 112 85 14", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "Kayplay.gamingroom@gmail.com", (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // --- 3. Numéro de ticket et date ---
        g2d.setFont(fontBold);
        centerString(g2d, "TICKET-" + numeroTicket, (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE dd/MM/yyyy HH:mm")), (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "Vendeur: " + userName, (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // --- 4. En-tête du tableau ---
        g2d.drawString("---------------------------------", 0, y);
        y += LINE_HEIGHT;
        g2d.drawString(String.format("%-5s %-15s %8s", "Qté", "Articles", "CFA"), 0, y);
        y += LINE_HEIGHT;
        g2d.drawString("---------------------------------", 0, y);
        y += LINE_HEIGHT;

        // --- 5. Liste des produits ---
        g2d.setFont(fontNormal);
        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            double prixUnitaire = produit.getPrix().doubleValue();
            double totalLigne = prixUnitaire * quantite;
            String nomProduit = produit.getNom();
            if (nomProduit.length() > 15) {
                nomProduit = nomProduit.substring(0, 12) + "...";
            }
            String line = String.format("%-5d %-15s %8.0f", quantite, nomProduit, totalLigne);
            g2d.drawString(line, 0, y);
            y += LINE_HEIGHT;
        }

        // --- 6. Total ---
        g2d.drawString("---------------------------------", 0, y);
        y += LINE_HEIGHT;
        g2d.setFont(fontBold);
        g2d.drawString(String.format("%-20s %8.0f", "TOTAL (CFA) :", montantTotal), 0, y);
        y += SECTION_SPACING;

        // --- 7. Mode de paiement ---
        g2d.setFont(fontNormal);
        g2d.drawString("Mode paiement: " + modePaiement, 0, y);
        y += SECTION_SPACING;

        // --- 8. Message personnalisé ---
        g2d.setFont(fontHeader);
        centerString(g2d, "KAY PLAY GAMING ROOM", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        g2d.setFont(fontNormal);
        centerString(g2d, "Votre passion pour les jeux prend vie !", (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // --- 9. Réseaux sociaux ---
        g2d.setFont(fontBold);
        centerString(g2d, "Suivez-nous sur", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        if (socialMediaImage != null) {
            int smWidth = 100;
            int smHeight = 20;
            int xSm = (int) ((RECEIPT_WIDTH_POINTS - smWidth) / 2);
            g2d.drawImage(socialMediaImage, xSm, y, smWidth, smHeight, null);
            y += smHeight + SECTION_SPACING;
        }

        // --- 10. Image Canal (si disponible) ---
        if (canalImage != null) {
            int imgWidth = 100;
            int imgHeight = (int) (canalImage.getHeight(null) * (double) imgWidth / canalImage.getWidth(null));
            int xImg = (int) ((RECEIPT_WIDTH_POINTS - imgWidth) / 2);
            g2d.drawImage(canalImage, xImg, y, imgWidth, imgHeight, null);
            y += imgHeight + SECTION_SPACING;
        }

        // --- 11. QR Code ---
        if (qrCodeImage != null) {
            int qrWidth = 70;
            int qrHeight = (int) (qrCodeImage.getHeight(null) * (double) qrWidth / qrCodeImage.getWidth(null));
            int xQr = (int) ((RECEIPT_WIDTH_POINTS - qrWidth) / 2);
            g2d.drawImage(qrCodeImage, xQr, y, qrWidth, qrHeight, null);
            y += qrHeight + SECTION_SPACING;
        }

        // --- 12. Mentions légales ---
        g2d.setFont(fontItalic);
        centerString(g2d, "Ticket non remboursable", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "À conserver pour toute réclamation", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        

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
        paper.setSize(RECEIPT_WIDTH_POINTS, RECEIPT_HEIGHT_POINTS);
        paper.setImageableArea(0, 0, RECEIPT_WIDTH_POINTS, RECEIPT_HEIGHT_POINTS);
        pf.setPaper(paper);
        job.setPrintable(this, pf);

        // --- Impression directe ---
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
                System.out.println("Reçu envoyé à l'imprimante : " + printerName);
            } catch (PrinterException e) {
                System.err.println("Erreur d'impression du reçu sur " + printerName + " : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Imprimante \"" + printerName + "\" non trouvée. Affichage du dialogue d'impression.");
            try {
                if (job.printDialog()) {
                    job.print();
                }
            } catch (PrinterException e) {
                System.err.println("Erreur lors de l'affichage du dialogue ou de l'impression : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
