package com.utils;

import com.entities.Produit;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Image;
import java.awt.print.Paper;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

public class ReceiptPrinter implements Printable {
    private Map<Produit, Integer> produitsDansLePanier;
    private double montantTotal;
    private String numeroTicket;
    private String userName;
    private static final double RECEIPT_WIDTH_POINTS = 220;
    private static final double RECEIPT_HEIGHT_POINTS = 72 * 18; // Augmenter la hauteur pour inclure plus de contenu
    private static final int LINE_HEIGHT = 10;
    private static final int SECTION_SPACING = 8;
    private Image logoImage;
    private Image socialMediaImage;
    private Image canalImage;
    private Image qrCodeImage;

    public ReceiptPrinter(Map<Produit, Integer> produitsDansLePanier, double montantTotal, String numeroTicket, String userName) {
        this.produitsDansLePanier = produitsDansLePanier;
        this.montantTotal = montantTotal;
        this.numeroTicket = numeroTicket;
        this.userName = userName;
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
            int logoWidth = 140;
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
        centerString(g2d, "Ticket N°: " + numeroTicket, (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "Vendeur: " + userName, (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // --- 4. Liste des produits ---
        g2d.setFont(fontBold);
        g2d.drawString(String.format("%-18s %6s %6s", "Article", "Qté", "Total"), 0, y);
        y += LINE_HEIGHT;
        g2d.drawString("---------------------------------", 0, y);
        y += LINE_HEIGHT;

        g2d.setFont(fontNormal);
        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            double prixUnitaire = produit.getPrix().doubleValue();
            double totalLigne = prixUnitaire * quantite;
            String nomProduit = produit.getNom();
            if (nomProduit.length() > 17) {
                nomProduit = nomProduit.substring(0, 14) + "...";
            }
            String line = String.format("%-18s %6d %6.2f", nomProduit, quantite, totalLigne);
            g2d.drawString(line, 0, y);
            y += LINE_HEIGHT;
        }

        y += LINE_HEIGHT;
        g2d.drawString("---------------------------------", 0, y);
        y += LINE_HEIGHT;

        g2d.setFont(fontBold);
        g2d.drawString(String.format("MONTANT TOTAL : %.2f FCFA", montantTotal), 0, y);
        y += SECTION_SPACING;

        // --- 5. Message de remerciement ---
        g2d.setFont(fontNormal);
        centerString(g2d, "Merci de votre achat !", (int) RECEIPT_WIDTH_POINTS, y);
        y += LINE_HEIGHT;
        centerString(g2d, "A très bientôt !", (int) RECEIPT_WIDTH_POINTS, y);
        y += SECTION_SPACING;

        // --- 6. Services supplémentaires ---
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

        // --- 7. Footer avec QR Code ---
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
        y += LINE_HEIGHT;
        centerString(g2d, "Contact: +221 77 137 45 53 26", (int) RECEIPT_WIDTH_POINTS, y);

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

        // --- DÉBUT DE LA MODIFICATION POUR L'IMPRESSION DIRECTE ---
        // 1. Définissez le nom exact de votre imprimante thermique.
        // Vous pouvez le trouver dans les "Périphériques et imprimantes" de Windows
        // ou les préférences système de macOS/Linux.
        String printerName = "Xprinter XP-T80Q"; // <-- REMPLACEZ PAR LE NOM EXACT DE VOTRE IMPRIMANTE
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
                job.setPrintService(selectedPrintService); // Définit l'imprimante à utiliser
                job.print(); // Lance l'impression directement sans dialogue
                System.out.println("Reçu envoyé à l'imprimante : " + printerName);
            } catch (PrinterException e) {
                System.err.println("Erreur d'impression du reçu sur " + printerName + " : " + e.getMessage());
                e.printStackTrace();
                // Optionnel : afficher une alerte JavaFX à l'utilisateur ici
            }
        } else {
            System.err.println("Imprimante \"" + printerName + "\" non trouvée. Affichage du dialogue d'impression.");
            // Si l'imprimante spécifique n'est pas trouvée, fallback sur le dialogue
            try {
                if (job.printDialog()) {
                    job.print();
                }
            } catch (PrinterException e) {
                System.err.println("Erreur lors de l'affichage du dialogue ou de l'impression : " + e.getMessage());
                e.printStackTrace();
            }
        }
        // --- FIN DE LA MODIFICATION POUR L'IMPRESSION DIRECTE ---
    }
}
