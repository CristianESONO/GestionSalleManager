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
    private static final double RECEIPT_HEIGHT_POINTS = 72 * 25;
    private static final int LINE_HEIGHT = 12;
    private static final int SECTION_SPACING = 10;
    private static final double MARGIN_LEFT = 15;
    private static final double MARGIN_RIGHT = 15;
    private static final double CONTENT_WIDTH = RECEIPT_WIDTH_POINTS - MARGIN_LEFT - MARGIN_RIGHT;
    private Image logoImage;
    private Image socialMediaImage;
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
            // Réduction de la taille du logo
            int logoWidth = (int) (CONTENT_WIDTH * 0.6); // 60% de la largeur
            int logoHeight = (int) (logoImage.getHeight(null) * (double) logoWidth / logoImage.getWidth(null));
            int xLogo = (int) (MARGIN_LEFT + (CONTENT_WIDTH - logoWidth) / 2);
            g2d.drawImage(logoImage, xLogo, y, logoWidth, logoHeight, null);
            y += logoHeight + SECTION_SPACING;
        }

        // --- 2. Informations de l'entreprise ---
        g2d.setFont(fontNormal);
        centerString(g2d, "Jaxaay, Parcelle Unité 24, BP 17000, KEUR MASSAR", y);
        y += LINE_HEIGHT;
        centerString(g2d, "Tel. +221 33 813 47 20 / 77 112 85 14", y);
        y += LINE_HEIGHT;
        centerString(g2d, "Kayplay.gamingroom@gmail.com", y);
        y += SECTION_SPACING + 5;

        // --- 3. Numéro de ticket et date ---
        g2d.setFont(fontBold);
        centerString(g2d, "TICKET-" + numeroTicket, y);
        y += LINE_HEIGHT;
        centerString(g2d, LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE dd/MM/yyyy HH:mm")), y);
        y += LINE_HEIGHT;
        centerString(g2d, "Vendeur: " + userName, y);
        y += SECTION_SPACING;

        // --- 4. En-tête du tableau ---
        String separator = "--------------------------------------------";
        g2d.drawString(separator, (int) MARGIN_LEFT, y);
        y += LINE_HEIGHT;
        
        // En-tête avec meilleure répartition des colonnes
        String header = String.format("%-8s %-20s %8s", "Qté", "Articles", "CFA");
        g2d.drawString(header, (int) MARGIN_LEFT, y);
        y += LINE_HEIGHT;
        
        g2d.drawString(separator, (int) MARGIN_LEFT, y);
        y += LINE_HEIGHT;

        // --- 5. Liste des produits ---
        g2d.setFont(fontNormal);
        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            double prixUnitaire = produit.getPrix().doubleValue();
            double totalLigne = prixUnitaire * quantite;
            String nomProduit = produit.getNom();
            
            // Tronquer le nom du produit si trop long
            if (nomProduit.length() > 20) {
                nomProduit = nomProduit.substring(0, 17) + "...";
            }
            
            // Format avec espacement optimisé
            String line = String.format("%-8d %-20s %8.0f", quantite, nomProduit, totalLigne);
            g2d.drawString(line, (int) MARGIN_LEFT, y);
            y += LINE_HEIGHT;
        }

        // --- 6. Total ---
        g2d.drawString(separator, (int) MARGIN_LEFT, y);
        y += LINE_HEIGHT;
        g2d.setFont(fontBold);
        String totalLine = String.format("%-28s %8.0f", "TOTAL (CFA) :", montantTotal);
        g2d.drawString(totalLine, (int) MARGIN_LEFT, y);
        y += SECTION_SPACING + 5;

        // --- 7. Mode de paiement ---
        g2d.setFont(fontNormal);
        g2d.drawString("Mode de paiement: " + modePaiement, (int) MARGIN_LEFT, y);
        y += SECTION_SPACING + 5;

        // --- 8. Message personnalisé ---
        g2d.setFont(fontHeader);
        centerString(g2d, "KAY PLAY GAMING ROOM", y);
        y += LINE_HEIGHT;
        g2d.setFont(fontNormal);
        centerString(g2d, "Votre passion pour les jeux prend vie !", y);
        y += SECTION_SPACING;

        // --- 9. Réseaux sociaux ---
        g2d.setFont(fontBold);
        centerString(g2d, "Suivez-nous sur", y);
        y += LINE_HEIGHT;
        if (socialMediaImage != null) {
            // Réduction de la taille de l'image des réseaux sociaux
            int smWidth = (int) (CONTENT_WIDTH * 0.7); // 70% de la largeur
            int smHeight = 25; // Hauteur fixe
            int xSm = (int) (MARGIN_LEFT + (CONTENT_WIDTH - smWidth) / 2);
            g2d.drawImage(socialMediaImage, xSm, y, smWidth, smHeight, null);
            y += smHeight + SECTION_SPACING;
        }

        // --- 10. QR Code ---
        if (qrCodeImage != null) {
            int qrWidth = 60; // Légèrement réduit
            int qrHeight = 60; // Carré
            int xQr = (int) (MARGIN_LEFT + (CONTENT_WIDTH - qrWidth) / 2);
            g2d.drawImage(qrCodeImage, xQr, y, qrWidth, qrHeight, null);
            y += qrHeight + SECTION_SPACING;
        }

        // --- 11. Mentions légales ---
        g2d.setFont(fontItalic);
        centerString(g2d, "Ticket non remboursable", y);
        y += LINE_HEIGHT;
        centerString(g2d, "À conserver pour toute réclamation", y);

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