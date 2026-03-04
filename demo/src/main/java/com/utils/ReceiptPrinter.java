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
    private static final double RECEIPT_HEIGHT_POINTS = 72 * 16;
    private static final int LINE_HEIGHT = 8;
    private static final int SECTION_SPACING = 5;
    private static final double MARGIN_LEFT = 10;
    private static final double MARGIN_RIGHT = 10;
    private static final double CONTENT_WIDTH = RECEIPT_WIDTH_POINTS - MARGIN_LEFT - MARGIN_RIGHT;
    private static final String SEPARATOR_LINE = "════════════════════════════════════════";
    private static final String SEPARATOR_THIN = "────────────────────────────────────────";
    private static final java.awt.Color COLOR_HEADER = new java.awt.Color(26, 54, 93);
    private static final java.awt.Color COLOR_TOTAL = new java.awt.Color(45, 125, 70);
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font fontNormal = new Font("SansSerif", Font.PLAIN, 7);
        Font fontBold = new Font("SansSerif", Font.BOLD, 8);
        Font fontHeader = new Font("SansSerif", Font.BOLD, 9);
        Font fontTicket = new Font("SansSerif", Font.BOLD, 8);
        Font fontItalic = new Font("SansSerif", Font.ITALIC, 6);

        int y = 0;

        // --- 1. LOGO (réduit) ---
        if (logoImage != null) {
            int logoWidth = (int) (CONTENT_WIDTH * 0.35);
            int logoHeight = (int) (logoImage.getHeight(null) * (double) logoWidth / logoImage.getWidth(null));
            if (logoHeight > 26) logoHeight = 26;
            int xLogo = (int) (MARGIN_LEFT + (CONTENT_WIDTH - logoWidth) / 2);
            g2d.drawImage(logoImage, xLogo, y, logoWidth, logoHeight, null);
            y += logoHeight + 3;
        }

        // --- 2. Entreprise (compact) ---
        g2d.setFont(fontHeader);
        g2d.setColor(COLOR_HEADER);
        centerString(g2d, "KAY PLAY GAMING ROOM", y);
        g2d.setColor(java.awt.Color.BLACK);
        y += LINE_HEIGHT;
        g2d.setFont(fontItalic);
        centerString(g2d, "Jaxaay, KEUR MASSAR", y);
        y += SECTION_SPACING;

        // --- 3. Numéro de ticket et date ---
        centerString(g2d, SEPARATOR_LINE, y);
        y += LINE_HEIGHT;
        g2d.setFont(fontTicket);
        centerString(g2d, "TICKET N° " + numeroTicket, y);
        y += LINE_HEIGHT;
        g2d.setFont(fontNormal);
        centerString(g2d, LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE dd/MM/yyyy · HH:mm")), y);
        y += LINE_HEIGHT;
        centerString(g2d, "Vendeur : " + userName, y);
        y += LINE_HEIGHT;
        centerString(g2d, SEPARATOR_LINE, y);
        y += SECTION_SPACING;

        // --- 4. Tableau des articles ---
        int colQteWidth = 28;
        int colArticleWidth = 108;
        int colPrixWidth = 44;
        int tableWidth = colQteWidth + colArticleWidth + colPrixWidth;
        int tableStartX = (int) (MARGIN_LEFT + (CONTENT_WIDTH - tableWidth) / 2);

        g2d.setFont(fontBold);
        g2d.drawString("Qté", tableStartX, y);
        g2d.drawString("Articles", tableStartX + colQteWidth, y);
        g2d.drawString("CFA", tableStartX + colQteWidth + colArticleWidth, y);
        y += LINE_HEIGHT;
        centerString(g2d, SEPARATOR_THIN, y);
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
            
            // Affichage des colonnes avec positions calculées
            g2d.drawString(String.valueOf(quantite), tableStartX, y);
            g2d.drawString(nomProduit, tableStartX + colQteWidth, y);
            g2d.drawString(String.format("%.0f", totalLigne), tableStartX + colQteWidth + colArticleWidth, y);
            y += LINE_HEIGHT;
        }

        // --- 6. Total (encadré visuel) ---
        centerString(g2d, SEPARATOR_THIN, y);
        y += LINE_HEIGHT;
        g2d.setFont(fontBold);
        g2d.setColor(COLOR_TOTAL);
        String totalText = "TOTAL (CFA) :";
        String totalValue = String.format("%.0f", montantTotal);
        int totalTextX = tableStartX + colQteWidth;
        int totalValueX = tableStartX + colQteWidth + colArticleWidth;
        g2d.drawString(totalText, totalTextX, y);
        g2d.drawString(totalValue, totalValueX, y);
        g2d.setColor(java.awt.Color.BLACK);
        y += LINE_HEIGHT;
        centerString(g2d, SEPARATOR_THIN, y);
        y += SECTION_SPACING;

        // --- 7. Mode de paiement ---
        g2d.setFont(fontNormal);
        centerString(g2d, "Paiement : " + modePaiement, y);
        y += LINE_HEIGHT;

        // --- 8. Mentions (compact) ---
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