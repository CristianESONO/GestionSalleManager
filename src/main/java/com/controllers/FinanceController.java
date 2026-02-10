package com.controllers;

import com.core.Fabrique;
import com.entities.AdminDailyReport;
import com.entities.Payment;
import com.entities.Produit;
import com.entities.Reservation;
import com.entities.Role;
import com.entities.User;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class FinanceController {

    // Palette pour les rapports PDF (RGB 0-1)
    private static final float PDF_COLOR_PRIMARY_R = 26f / 255f;
    private static final float PDF_COLOR_PRIMARY_G = 54f / 255f;
    private static final float PDF_COLOR_PRIMARY_B = 93f / 255f;
    private static final float PDF_COLOR_SUCCESS_R = 45f / 255f;
    private static final float PDF_COLOR_SUCCESS_G = 125f / 255f;
    private static final float PDF_COLOR_SUCCESS_B = 70f / 255f;
    private static final float PDF_COLOR_HEADER_BG_R = 0.25f;
    private static final float PDF_COLOR_HEADER_BG_G = 0.35f;
    private static final float PDF_COLOR_HEADER_BG_B = 0.55f;

    @FXML private Text titleText;
    @FXML private VBox adminInfoBox;
    @FXML private Label adminNameLabel;
    @FXML private Label dateTimeLabel;
    @FXML private TabPane mainTabPane;
    @FXML private Tab ventesTab;
    @FXML private Tab stockTab;
    @FXML private Tab journalTab;
    @FXML private TableView<Object> ventesTable;
    @FXML private TableColumn<Object, String> numeroTicketVenteColumn;
    @FXML private TableColumn<Object, String> clientVenteColumn;
    @FXML private TableColumn<Object, String> montantTotalVenteColumn;
    @FXML private TableColumn<Object, String> modePaiementVenteColumn;
    @FXML private TableColumn<Object, String> dateHeureVenteColumn;
    @FXML private TableView<Produit> stockTable;
    @FXML private TableColumn<Produit, String> nomProduitStockColumn;
    @FXML private TableColumn<Produit, String> prixProduitStockColumn;
    @FXML private TableColumn<Produit, String> stockProduitStockColumn;
    @FXML private TableColumn<Produit, String> dateAjoutStockColumn;
    @FXML private TableView<Object> journalTable;
    @FXML private TableColumn<Object, String> journalTypeColumn;
    @FXML private TableColumn<Object, String> journalDescriptionColumn;
    @FXML private TableColumn<Object, String> journalDateColumn;
    @FXML private TableColumn<Object, String> journalMontantColumn;
    @FXML private TableColumn<Object, String> journalAdminColumn;
    @FXML private TableColumn<Object, Void> journalCheckColumn;
    @FXML private TableColumn<Object, Void> journalActionsColumn;
    @FXML private TextField searchVenteField;
    @FXML private TextField searchStockField;
    @FXML private TextField searchJournalField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Pagination ventesPagination;
    @FXML private Pagination stockPagination;
    @FXML private Pagination journalPagination;
    @FXML private Label totalVentesLabel;
    @FXML private Label totalReservationsLabel;
    private ObservableList<Object> ventesList;
    private ObservableList<Produit> stockList;
    private ObservableList<Object> journalList;
    private ObservableList<Object> filteredJournalList;
    private static final int ITEMS_PER_PAGE = 25;
    private boolean isAdmin;
    private User currentUser;
    // Dans la classe FinanceController, ajoute ces attributs :
    private ObservableList<Payment> ventesProduitsList;
    private ObservableList<Reservation> reservationsList;

    @FXML
    public void initialize() {
        currentUser = Fabrique.getService().getCurrentUser();
        isAdmin = currentUser.getRole() == Role.Admin;
        configureColumns();
        ventesList = FXCollections.observableArrayList();
        stockList = FXCollections.observableArrayList();
        journalList = FXCollections.observableArrayList();
        filteredJournalList = FXCollections.observableArrayList();
        // Dans initialize(), initialise-les :
        ventesProduitsList = FXCollections.observableArrayList();
        reservationsList = FXCollections.observableArrayList();
        loadVentes();
        loadStock();
        loadJournal();
        searchVenteField.textProperty().addListener((obs, oldVal, newVal) -> filterVentes(newVal));
        searchStockField.textProperty().addListener((obs, oldVal, newVal) -> filterStock(newVal));
        searchJournalField.textProperty().addListener((obs, oldVal, newVal) -> filterJournal(newVal));
        ventesPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updateVentesPagination(newIndex.intValue()));
        stockPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updateStockPagination(newIndex.intValue()));
        journalPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updateJournalPagination(newIndex.intValue()));
        journalTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        journalTable.setStyle(""
            + "-fx-selection-bar: #3498db;"
            + "-fx-selection-bar-non-focused: #b8e1ff;"
            + "-fx-cell-focus-inner-border: transparent;"
            + "-fx-background-insets: 0, 0 0 1 0;");
        applyRoleBasedView(currentUser);
        calculateTotals();

      // Utilisez :
        ventesList.addListener((ListChangeListener<Object>) change -> {
            Platform.runLater(() -> {
                ventesPagination.setPageCount((int) Math.ceil(ventesList.size() / (double) ITEMS_PER_PAGE));
                ventesPagination.setCurrentPageIndex(0);
                updateVentesPagination(0);
            });
        });

        stockList.addListener((ListChangeListener<Object>) change -> {
            Platform.runLater(() -> {
                stockPagination.setPageCount((int) Math.ceil(stockList.size() / (double) ITEMS_PER_PAGE));
                stockPagination.setCurrentPageIndex(0);
                updateStockPagination(0);
            });
        });

        journalList.addListener((ListChangeListener<Object>) change -> {
            Platform.runLater(() -> {
                journalPagination.setPageCount((int) Math.ceil(journalList.size() / (double) ITEMS_PER_PAGE));
                journalPagination.setCurrentPageIndex(0);
                updateJournalPagination(0);
            });
        });

        filteredJournalList.addListener((ListChangeListener<Object>) change -> {
            Platform.runLater(() -> {
                journalPagination.setPageCount((int) Math.ceil(filteredJournalList.size() / (double) ITEMS_PER_PAGE));
                journalPagination.setCurrentPageIndex(0);
                updateJournalPagination(0);
            });
        });
    }

    private boolean isReservationPayment(Payment payment) {
        return payment.getDetailReservations() != null && !payment.getDetailReservations().trim().isEmpty();
    }

    private boolean isVenteProduitPayment(Payment payment) {
        return payment.getDetailsProduits() != null && !payment.getDetailsProduits().trim().isEmpty();
    }

    private void refreshData() {
        Platform.runLater(() -> {
            loadVentes();
            loadStock();
            loadJournal();
        });
    }


    private void configureColumns() {
        numeroTicketVenteColumn.prefWidthProperty().bind(ventesTable.widthProperty().multiply(0.20));
        clientVenteColumn.prefWidthProperty().bind(ventesTable.widthProperty().multiply(0.20));
        montantTotalVenteColumn.prefWidthProperty().bind(ventesTable.widthProperty().multiply(0.15));
        modePaiementVenteColumn.prefWidthProperty().bind(ventesTable.widthProperty().multiply(0.15));
        dateHeureVenteColumn.prefWidthProperty().bind(ventesTable.widthProperty().multiply(0.20));
        numeroTicketVenteColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Payment) {
                return new SimpleStringProperty(((Payment) cellData.getValue()).getNumeroTicket());
            } else if (cellData.getValue() instanceof Reservation) {
                return new SimpleStringProperty(((Reservation) cellData.getValue()).getNumeroTicket());
            }
            return new SimpleStringProperty("");
        });
        clientVenteColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Payment) {
                Payment payment = (Payment) cellData.getValue();
                if (isReservationPayment(payment)) {
                    // Pour les réservations : afficher le vrai nom du client
                    return new SimpleStringProperty(payment.getClient() != null ? payment.getClient().getName() : "N/A");
                } else {
                    // Pour les ventes de produits : afficher "Inconnu"
                    return new SimpleStringProperty("Inconnu");
                }
            } else if (cellData.getValue() instanceof Reservation) {
                Reservation reservation = (Reservation) cellData.getValue();
                return new SimpleStringProperty(reservation.getClient() != null ? reservation.getClient().getName() : "N/A");
            }
            return new SimpleStringProperty("");
        });

        montantTotalVenteColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Payment) {
                return new SimpleStringProperty(String.format("%.2f", ((Payment) cellData.getValue()).getMontantTotal()));
            } else if (cellData.getValue() instanceof Reservation) {
                return new SimpleStringProperty(String.format("%.2f", ((Reservation) cellData.getValue()).getTotalPrice()));
            }
            return new SimpleStringProperty("");
        });
        modePaiementVenteColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Payment) {
                Payment payment = (Payment) cellData.getValue();
                // Toujours afficher le mode de paiement réel
                return new SimpleStringProperty(payment.getModePaiement());
            } else if (cellData.getValue() instanceof Reservation) {
                // Pour les réservations affichées directement
                return new SimpleStringProperty("Réservation");
            }
            return new SimpleStringProperty("");
        });
        dateHeureVenteColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Payment) {
                return new SimpleStringProperty(((Payment) cellData.getValue()).getDateHeure().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            } else if (cellData.getValue() instanceof Reservation) {
                return new SimpleStringProperty(((Reservation) cellData.getValue()).getReservationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            }
            return new SimpleStringProperty("");
        });
        nomProduitStockColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNom()));
        prixProduitStockColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getPrix())));
        stockProduitStockColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getStock())));
        dateAjoutStockColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateLimiteConsommation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        journalTypeColumn.setCellValueFactory(cellData -> {
            AdminDailyReport report = (AdminDailyReport) cellData.getValue();
            return new SimpleStringProperty("Rapport Journalier");
        });
        journalDescriptionColumn.setCellValueFactory(cellData -> {
            AdminDailyReport report = (AdminDailyReport) cellData.getValue();
            return new SimpleStringProperty(
                String.format("Admin: %s, Date: %s, Opérations: %d",
                    report.getAdmin().getName(),
                    report.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    report.getTotalOperations()
                )
            );
        });
        journalDateColumn.setCellValueFactory(cellData -> {
            AdminDailyReport report = (AdminDailyReport) cellData.getValue();
            return new SimpleStringProperty(report.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
        journalMontantColumn.setCellValueFactory(cellData -> {
            AdminDailyReport report = (AdminDailyReport) cellData.getValue();
            return new SimpleStringProperty(String.format("%.2f", report.getTotalAmount()));
        });
        journalAdminColumn.setCellValueFactory(cellData -> {
            AdminDailyReport report = (AdminDailyReport) cellData.getValue();
            return new SimpleStringProperty(report.getAdmin().getName());
        });
        journalCheckColumn.setCellFactory(tc -> new TableCell<Object, Void>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(event -> {
                    Object item = getTableView().getItems().get(getIndex());
                    if (checkBox.isSelected()) {
                        journalTable.getSelectionModel().select(item);
                    } else {
                        journalTable.getSelectionModel().clearSelection();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(checkBox);
                    checkBox.setSelected(journalTable.getSelectionModel().isSelected(getIndex()));
                }
            }
        });
        journalTable.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    row.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                } else {
                    row.setStyle("");
                }
                if (!row.isEmpty()) {
                    TableCell<Object, Void> cell = (TableCell<Object, Void>) row.getChildrenUnmodifiable().get(0);
                    if (cell != null && cell.getGraphic() instanceof CheckBox) {
                        ((CheckBox) cell.getGraphic()).setSelected(isNowSelected);
                    }
                }
            });
            return row;
        });
        journalActionsColumn.setCellFactory(tc -> new TableCell<Object, Void>() {
            final Button viewButton = new Button("Voir");
            final Button generatePdfButton = new Button("Générer PDF");
            final HBox pane = new HBox(5, viewButton, generatePdfButton);
            {
                viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;");
                generatePdfButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12px; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;");
                viewButton.setOnAction(event -> {
                    Object item = getTableView().getItems().get(getIndex());
                    generateAndOpenPdf(item, false);
                });
                generatePdfButton.setOnAction(event -> {
                    Object item = getTableView().getItems().get(getIndex());
                    generateAndOpenPdf(item, true);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void applyRoleBasedView(User currentUser) {
        ventesTab.setDisable(true);
        ventesTab.getTabPane().getTabs().remove(ventesTab);
        stockTab.setDisable(true);
        stockTab.getTabPane().getTabs().remove(stockTab);
        journalTab.setDisable(true);
        journalTab.getTabPane().getTabs().remove(journalTab);
        if (searchJournalField != null) {
            searchJournalField.setVisible(false);
            searchJournalField.setManaged(false);
        }
        if (startDatePicker != null) {
            startDatePicker.setVisible(false);
            startDatePicker.setManaged(false);
        }
        if (endDatePicker != null) {
            endDatePicker.setVisible(false);
            endDatePicker.setManaged(false);
        }
        if (currentUser.getRole() == Role.SuperAdmin) {
            mainTabPane.getTabs().add(stockTab);
            mainTabPane.getTabs().add(journalTab);
            stockTab.setDisable(false);
            journalTab.setDisable(false);
            adminInfoBox.setVisible(false);
            adminInfoBox.setManaged(false);
            if (searchJournalField != null) {
                searchJournalField.setVisible(true);
                searchJournalField.setManaged(true);
            }
            if (startDatePicker != null) {
                startDatePicker.setVisible(true);
                startDatePicker.setManaged(true);
            }
            if (endDatePicker != null) {
                endDatePicker.setVisible(true);
                endDatePicker.setManaged(true);
            }
            mainTabPane.getSelectionModel().select(journalTab);
        } else if (currentUser.getRole() == Role.Admin) {
            mainTabPane.getTabs().add(ventesTab);
            ventesTab.setDisable(false);
            adminInfoBox.setVisible(true);
            adminInfoBox.setManaged(true);
            adminNameLabel.setText("Connecté : " + currentUser.getName());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            dateTimeLabel.setText("Date & Heure : " + LocalDateTime.now().format(formatter));
            ventesTable.setEditable(false);
            searchStockField.setDisable(true);
            mainTabPane.getSelectionModel().select(ventesTab);
        }
    }

private void loadVentes() {
    if (isAdmin) {
        LocalDate today = LocalDate.now();
        User currentUser = Fabrique.getService().getCurrentUser();
        // Récupérer TOUS les paiements de l'admin connecté pour la journée
        List<Payment> paiements = Fabrique.getService().getPaymentsByUser(currentUser)
            .stream()
            .filter(p -> p.getDateHeure().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(today))
            .collect(Collectors.toList());
        // Ajouter TOUS les paiements (ventes de produits + paiements de réservations)
        ventesList.clear();
        ventesList.addAll(paiements);
        ventesPagination.setPageCount((int) Math.ceil(ventesList.size() / (double) ITEMS_PER_PAGE));
        updateVentesPagination(0);
        calculateTotals();
    }
}


    private void loadStock() {
        if (!isAdmin) {
            List<Produit> produits = Fabrique.getService().findAllProduits();
            stockList.clear();
            stockList.addAll(produits);
            stockPagination.setPageCount((int) Math.ceil(stockList.size() / (double) ITEMS_PER_PAGE));
            updateStockPagination(0);
        }
    }

    private void loadJournal() {
        if (!isAdmin) {
            journalList.clear();
            Map<User, Map<LocalDate, List<Object>>> adminDailyOperations = new HashMap<>();
            List<Object> allOperations = new ArrayList<>();
            allOperations.addAll(Fabrique.getService().getAllPayments());
            allOperations.addAll(Fabrique.getService().findAllReservations());
            for (Object op : allOperations) {
                User createdBy = null;
                LocalDate date = null;
                if (op instanceof Payment) {
                    createdBy = ((Payment) op).getCreatedBy();
                    date = ((Payment) op).getDateHeure().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                } else if (op instanceof Reservation) {
                    createdBy = ((Reservation) op).getCreatedBy();
                    date = ((Reservation) op).getReservationDate().toLocalDate();
                }
                if (createdBy != null && date != null) {
                    adminDailyOperations.putIfAbsent(createdBy, new HashMap<>());
                    adminDailyOperations.get(createdBy).putIfAbsent(date, new ArrayList<>());
                    adminDailyOperations.get(createdBy).get(date).add(op);
                }
            }
            for (Map.Entry<User, Map<LocalDate, List<Object>>> adminEntry : adminDailyOperations.entrySet()) {
                User admin = adminEntry.getKey();
                for (Map.Entry<LocalDate, List<Object>> dateEntry : adminEntry.getValue().entrySet()) {
                    LocalDate date = dateEntry.getKey();
                    List<Object> operations = dateEntry.getValue();
                    // Dans loadJournal, quand tu construis les listes :
                    List<Payment> payments = operations.stream()
                        .filter(op -> op instanceof Payment)
                        .map(op -> (Payment) op)
                        .filter(p -> p.getDetailsProduits() != null && !p.getDetailsProduits().trim().isEmpty()) // Uniquement les ventes de produits
                        .collect(Collectors.toList());

                    List<Reservation> reservations = operations.stream()
                        .filter(op -> op instanceof Reservation)
                        .map(op -> (Reservation) op)
                        .collect(Collectors.toList());

                    AdminDailyReport report = new AdminDailyReport(admin, date, payments, reservations);
                    journalList.add(report);
                }
            }
            journalList.sort((o1, o2) -> {
                AdminDailyReport r1 = (AdminDailyReport) o1;
                AdminDailyReport r2 = (AdminDailyReport) o2;
                return r2.getDate().compareTo(r1.getDate());
            });
            filteredJournalList.addAll(journalList);
            journalPagination.setPageCount((int) Math.ceil(filteredJournalList.size() / (double) ITEMS_PER_PAGE));
            updateJournalPagination(0);
        }
    }

    private void filterVentes(String searchText) {
    if (!isAdmin) return;
    List<Payment> paiements = Fabrique.getService().getAllPayments().stream()
        .filter(p -> p.getDateHeure().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(LocalDate.now()))
        .collect(Collectors.toList());
    List<Object> filteredList = paiements.stream()
        .filter(payment -> {
            return payment.getNumeroTicket().toLowerCase().contains(searchText.toLowerCase()) ||
                   (payment.getClient() != null && payment.getClient().getName().toLowerCase().contains(searchText.toLowerCase())) ||
                   payment.getModePaiement().toLowerCase().contains(searchText.toLowerCase());
        })
        .collect(Collectors.toList());
    ventesList.setAll(filteredList);
    ventesPagination.setPageCount((int) Math.ceil(ventesList.size() / (double) ITEMS_PER_PAGE));
    ventesPagination.setCurrentPageIndex(0);
    updateVentesPagination(0);
}


    private void filterStock(String searchText) {
        if (isAdmin) return;
        List<Produit> currentStock = Fabrique.getService().findAllProduits();
        List<Produit> filteredList = currentStock.stream()
            .filter(produit -> produit.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                                String.valueOf(produit.getPrix()).contains(searchText.toLowerCase()))
            .collect(Collectors.toList());
        stockList.setAll(filteredList);
        stockPagination.setPageCount((int) Math.ceil(stockList.size() / (double) ITEMS_PER_PAGE));
        stockPagination.setCurrentPageIndex(0);
        updateStockPagination(0);
    }

    @FXML
    private void filterJournal(String searchText) {
        if (isAdmin) return;
        List<Object> tempFilteredList = journalList.stream()
            .filter(item -> {
                AdminDailyReport report = (AdminDailyReport) item;
                String adminName = report.getAdmin().getName().toLowerCase();
                String dateStr = report.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toLowerCase();
                String totalAmountStr = String.format("%.2f", report.getTotalAmount()).toLowerCase();
                String operationsCountStr = String.valueOf(report.getTotalOperations()).toLowerCase();
                String lowerSearchText = searchText.toLowerCase();
                return adminName.contains(lowerSearchText) ||
                       dateStr.contains(lowerSearchText) ||
                       totalAmountStr.contains(lowerSearchText) ||
                       operationsCountStr.contains(lowerSearchText);
            })
            .collect(Collectors.toList());
        filteredJournalList.setAll(tempFilteredList);
        journalPagination.setPageCount((int) Math.ceil(filteredJournalList.size() / (double) ITEMS_PER_PAGE));
        journalPagination.setCurrentPageIndex(0);
        updateJournalPagination(0);
    }

    @FXML
    private void filterJournalByDate(ActionEvent event) {
        if (isAdmin) return;
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        List<Object> tempFilteredList = journalList.stream()
            .filter(item -> {
                AdminDailyReport report = (AdminDailyReport) item;
                boolean afterStart = (startDate == null) || !report.getDate().isBefore(startDate);
                boolean beforeEnd = (endDate == null) || !report.getDate().isAfter(endDate);
                return afterStart && beforeEnd;
            })
            .collect(Collectors.toList());
        filteredJournalList.setAll(tempFilteredList);
        journalPagination.setPageCount((int) Math.ceil(filteredJournalList.size() / (double) ITEMS_PER_PAGE));
        journalPagination.setCurrentPageIndex(0);
        updateJournalPagination(0);
    }

    private void updateVentesPagination(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, ventesList.size());
        ventesTable.setItems(FXCollections.observableArrayList(ventesList.subList(fromIndex, toIndex)));
    }

    private void updateStockPagination(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, stockList.size());
        stockTable.setItems(FXCollections.observableArrayList(stockList.subList(fromIndex, toIndex)));
    }

    private void updateJournalPagination(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredJournalList.size());
        journalTable.setItems(FXCollections.observableArrayList(filteredJournalList.subList(fromIndex, toIndex)));
    }

    private void calculateTotals() {
        if (isAdmin) {
            double totalVentesProduits = ventesList.stream()
                .filter(item -> item instanceof Payment)
                .map(item -> (Payment) item)
                .filter(payment -> isVenteProduitPayment(payment))
                .mapToDouble(Payment::getMontantTotal)
                .sum();

            double totalReservations = ventesList.stream()
                .filter(item -> item instanceof Payment)
                .map(item -> (Payment) item)
                .filter(payment -> isReservationPayment(payment))
                .mapToDouble(Payment::getMontantTotal)
                .sum();

            totalVentesLabel.setText(String.format("Total Ventes Produits (jour) : %.2f F", totalVentesProduits));
            totalReservationsLabel.setText(String.format("Total Réservations (jour) : %.2f F", totalReservations));
        }
    }


@FXML
private void generateDailyPdf() {
    if (!isAdmin) return;
    LocalDate today = LocalDate.now();

    // Récupère les opérations du jour
    List<Payment> dailyPayments = Fabrique.getService().getAllPayments().stream()
    .filter(p -> p.getDateHeure().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(today))
    .filter(p -> p.getDetailsProduits() != null && !p.getDetailsProduits().trim().isEmpty()) // Uniquement les ventes de produits
    .collect(Collectors.toList());


    List<Reservation> dailyReservations = Fabrique.getService().findAllReservations().stream()
    .filter(r -> r.getReservationDate().toLocalDate().isEqual(today))
    .collect(Collectors.toList());


    if (dailyPayments.isEmpty() && dailyReservations.isEmpty()) {
        showAlert(Alert.AlertType.INFORMATION, "Aucune Opération", "Il n'y a aucune opération pour aujourd'hui.");
        return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Enregistrer le rapport journalier");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
    fileChooser.setInitialFileName("Rapport_Journalier_" + today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".pdf");
    File file = fileChooser.showSaveDialog(new Stage());

    if (file != null) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            float leading = 25;

            // --- Bandeau d'en-tête (bleu Kay Play) ---
            float headerHeight = 52;
            contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
            contentStream.addRect(0, yStart - headerHeight, page.getMediaBox().getWidth(), headerHeight);
            contentStream.fill();
            contentStream.setNonStrokingColor(1f, 1f, 1f);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            String headerTitle = "KAY PLAY GAMING ROOM";
            float headerTitleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(headerTitle) / 1000 * 20;
            contentStream.beginText();
            contentStream.newLineAtOffset((page.getMediaBox().getWidth() - headerTitleWidth) / 2, yStart - headerHeight + 18);
            contentStream.showText(headerTitle);
            contentStream.endText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            String headerSub = "Rapport financier · " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            float headerSubWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(headerSub) / 1000 * 10;
            contentStream.beginText();
            contentStream.newLineAtOffset((page.getMediaBox().getWidth() - headerSubWidth) / 2, yStart - headerHeight + 4);
            contentStream.showText(headerSub);
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);
            yPosition -= headerHeight + leading;

            // Infos entreprise (sous le bandeau)
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
            String[] companyInfos = {
                "Jaxaay, Parcelle Unité 24, BP 17000, KEUR MASSAR · Tel. +221 338134720 / 771128514",
                "Kayplay.gamingroom@gmail.com · NINEA 012344800"
            };
            for (String info : companyInfos) {
                float infoWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(info) / 1000 * 9;
                contentStream.beginText();
                contentStream.newLineAtOffset((page.getMediaBox().getWidth() - infoWidth) / 2, yPosition);
                contentStream.showText(info);
                contentStream.endText();
                yPosition -= 12;
            }
            yPosition -= 8;

            // Titre du rapport
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
            String rapportTitle = "Rapport Journalier — " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            float titleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(rapportTitle) / 1000 * 14;
            contentStream.beginText();
            contentStream.newLineAtOffset((page.getMediaBox().getWidth() - titleWidth) / 2, yPosition);
            contentStream.showText(rapportTitle);
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);
            yPosition -= 16;

            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            String adminText = "Généré par : " + currentUser.getName() + " · " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            float adminWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(adminText) / 1000 * 10;
            contentStream.beginText();
            contentStream.newLineAtOffset((page.getMediaBox().getWidth() - adminWidth) / 2, yPosition);
            contentStream.showText(adminText);
            contentStream.endText();
            yPosition -= 20;

            // --- Ventes de Produits ---
            if (!dailyPayments.isEmpty()) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Ventes de Produits");
                contentStream.endText();
                contentStream.setNonStrokingColor(0f, 0f, 0f);
                contentStream.setStrokingColor(0.85f, 0.85f, 0.85f);
                contentStream.moveTo(margin, yPosition - 4);
                contentStream.lineTo(margin + 120, yPosition - 4);
                contentStream.stroke();
                contentStream.setStrokingColor(0f, 0f, 0f);
                yPosition -= leading;

                // Tableau des ventes de produits
                float[] paymentColumnWidths = {0.20f, 0.20f, 0.15f, 0.15f, 0.20f};
                yPosition = drawTable(
                    document, contentStream, page, yPosition, margin, paymentColumnWidths,
                    new String[]{"N° Ticket", "Client", "Montant", "Mode Paiement", "Heure"},
                    dailyPayments,
                    (item, cs, startX, startY, columnWidths) -> {
                        Payment p = (Payment) item;
                        cs.showText(p.getNumeroTicket());
                        cs.newLineAtOffset(columnWidths[0], 0);
                        cs.showText(p.getClient() != null ? p.getClient().getName() : "N/A");
                        cs.newLineAtOffset(columnWidths[1], 0);
                        cs.showText(String.format("%.2f", p.getMontantTotal()));
                        cs.newLineAtOffset(columnWidths[2], 0);
                        cs.showText(p.getModePaiement());
                        cs.newLineAtOffset(columnWidths[3], 0);
                        cs.showText(p.getDateHeure().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                );

                // --- Détail des articles achetés par vente (rapport détaillé) ---
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Détail des articles achetés :");
                contentStream.endText();
                yPosition -= leading;
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                for (Payment p : dailyPayments) {
                    String details = p.getDetailsProduits();
                    if (details != null && !details.trim().isEmpty()) {
                        String ticketLine = p.getNumeroTicket() + " :";
                        if (ticketLine.length() > 45) ticketLine = ticketLine.substring(0, 42) + "...";
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText(ticketLine);
                        contentStream.endText();
                        yPosition -= leading;
                        for (String part : details.split(",\\s*")) {
                            String line = "  - " + part.trim();
                            if (line.length() > 70) line = line.substring(0, 67) + "...";
                            contentStream.beginText();
                            contentStream.newLineAtOffset(margin, yPosition);
                            contentStream.showText(line);
                            contentStream.endText();
                            yPosition -= leading;
                            if (yPosition < margin + 40) {
                                contentStream.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                contentStream = new PDPageContentStream(document, page);
                                yPosition = page.getMediaBox().getHeight() - margin;
                            }
                        }
                        yPosition -= 4;
                    }
                }
                yPosition -= 8;

                // Ligne de séparation avant le total
                contentStream.setStrokingColor(0.7f, 0.7f, 0.7f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                contentStream.setStrokingColor(0f, 0f, 0f);
                yPosition -= 15; // Espace supplémentaire avant le total

                // Total des ventes de produits
                double totalVentes = dailyPayments.stream().mapToDouble(Payment::getMontantTotal).sum();
                String totalVentesText = String.format("Total Ventes Produits : %.2f F", totalVentes);
                float totalVentesWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(totalVentesText) / 1000 * 12;
                float totalVentesX = (page.getMediaBox().getWidth() - totalVentesWidth) / 2;
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.setNonStrokingColor(PDF_COLOR_SUCCESS_R, PDF_COLOR_SUCCESS_G, PDF_COLOR_SUCCESS_B);
                contentStream.beginText();
                contentStream.newLineAtOffset(totalVentesX, yPosition);
                contentStream.showText(totalVentesText);
                contentStream.endText();
                contentStream.setNonStrokingColor(0f, 0f, 0f);
                yPosition -= leading * 3;
            }

            // --- Réservations ---
            if (!dailyReservations.isEmpty()) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Réservations");
                contentStream.endText();
                contentStream.setNonStrokingColor(0f, 0f, 0f);
                contentStream.setStrokingColor(0.85f, 0.85f, 0.85f);
                contentStream.moveTo(margin, yPosition - 4);
                contentStream.lineTo(margin + 90, yPosition - 4);
                contentStream.stroke();
                contentStream.setStrokingColor(0f, 0f, 0f);
                yPosition -= leading;

                // Tableau des réservations
                float[] reservationColumnWidths = {0.20f, 0.20f, 0.15f, 0.15f, 0.20f};
                yPosition = drawTable(
                    document, contentStream, page, yPosition, margin, reservationColumnWidths,
                    new String[]{"N° Ticket", "Client", "Montant", "Poste", "Heure"},
                    dailyReservations,
                    (item, cs, startX, startY, columnWidths) -> {
                        Reservation r = (Reservation) item;
                        cs.showText(r.getNumeroTicket());
                        cs.newLineAtOffset(columnWidths[0], 0);
                        cs.showText(r.getClient() != null ? r.getClient().getName() : "N/A");
                        cs.newLineAtOffset(columnWidths[1], 0);
                        cs.showText(String.format("%.2f", r.getTotalPrice()));
                        cs.newLineAtOffset(columnWidths[2], 0);
                        cs.showText(r.getPoste() != null ? r.getPoste().getName() : "N/A");
                        cs.newLineAtOffset(columnWidths[3], 0);
                        cs.showText(r.getReservationDate().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                );

                // Ligne de séparation avant le total
                contentStream.setStrokingColor(0.7f, 0.7f, 0.7f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                contentStream.setStrokingColor(0f, 0f, 0f);
                yPosition -= 15; // Espace supplémentaire avant le total

                // Total des réservations (centré et espacé)
                double totalReservations = dailyReservations.stream().mapToDouble(Reservation::getTotalPrice).sum();
                String totalReservationsText = String.format("Total Réservations : %.2f F", totalReservations);
                float totalReservationsWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(totalReservationsText) / 1000 * 12;
                float totalReservationsX = (page.getMediaBox().getWidth() - totalReservationsWidth) / 2;

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.setNonStrokingColor(PDF_COLOR_SUCCESS_R, PDF_COLOR_SUCCESS_G, PDF_COLOR_SUCCESS_B);
                contentStream.beginText();
                contentStream.newLineAtOffset(totalReservationsX, yPosition);
                contentStream.showText(totalReservationsText);
                contentStream.endText();
                contentStream.setNonStrokingColor(0f, 0f, 0f);
                yPosition -= leading * 3;
            }

            yPosition -= 12;

            // --- Grand Total Journalier (encadré) ---
            double totalVentes = dailyPayments.stream().mapToDouble(Payment::getMontantTotal).sum();
            double totalReservations = dailyReservations.stream().mapToDouble(Reservation::getTotalPrice).sum();
            double grandTotal = totalVentes + totalReservations;
            String grandTotalText = String.format("Grand Total Journalier : %.2f F", grandTotal);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            float grandTotalWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(grandTotalText) / 1000 * 16;
            float grandTotalX = (page.getMediaBox().getWidth() - grandTotalWidth) / 2;
            float boxPad = 12;
            contentStream.setNonStrokingColor(0.94f, 0.95f, 0.97f);
            contentStream.addRect(grandTotalX - boxPad, yPosition - 22, grandTotalWidth + 2 * boxPad, 28);
            contentStream.fill();
            contentStream.setStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
            contentStream.setLineWidth(1.2f);
            contentStream.addRect(grandTotalX - boxPad, yPosition - 22, grandTotalWidth + 2 * boxPad, 28);
            contentStream.stroke();
            contentStream.setStrokingColor(0f, 0f, 0f);
            contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
            contentStream.beginText();
            contentStream.newLineAtOffset(grandTotalX, yPosition - 16);
            contentStream.showText(grandTotalText);
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);

            contentStream.close();
            document.save(file);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport PDF généré avec succès !");
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur PDF", "Impossible de générer le rapport PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }
}



  private void generateAndOpenPdf(Object item, boolean saveToFile) {
    if (!(item instanceof AdminDailyReport)) {
        showAlert(Alert.AlertType.ERROR, "Erreur", "L'élément sélectionné n'est pas un rapport journalier.");
        return;
    }

    AdminDailyReport report = (AdminDailyReport) item;
    File file = null;

    // Choix du fichier de sortie
    if (saveToFile) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport journalier");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName(
            String.format("Rapport_%s_%s.pdf",
                report.getAdmin().getName(),
                report.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            )
        );
        file = fileChooser.showSaveDialog(new Stage());
        if (file == null) return;
    } else {
        try {
            file = File.createTempFile("temp_rapport_", ".pdf");
            file.deleteOnExit();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer le fichier temporaire: " + e.getMessage());
            return;
        }
    }

    try (PDDocument document = new PDDocument()) {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        float margin = 50;
        float yStart = page.getMediaBox().getHeight() - margin;
        float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
        float yPosition = yStart;
        float leading = 20;

        // --- Bandeau d'en-tête ---
        float headerHeight = 52;
        contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
        contentStream.addRect(0, yStart - headerHeight, page.getMediaBox().getWidth(), headerHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(1f, 1f, 1f);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
        String headerTitle = "KAY PLAY GAMING ROOM";
        float headerTitleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(headerTitle) / 1000 * 20;
        contentStream.beginText();
        contentStream.newLineAtOffset((page.getMediaBox().getWidth() - headerTitleWidth) / 2, yStart - headerHeight + 18);
        contentStream.showText(headerTitle);
        contentStream.endText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        String headerSub = "Rapport journalier — " + report.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        float headerSubWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(headerSub) / 1000 * 10;
        contentStream.beginText();
        contentStream.newLineAtOffset((page.getMediaBox().getWidth() - headerSubWidth) / 2, yStart - headerHeight + 4);
        contentStream.showText(headerSub);
        contentStream.endText();
        contentStream.setNonStrokingColor(0f, 0f, 0f);
        yPosition -= headerHeight + 20;

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
        String adminLine = "Admin : " + report.getAdmin().getName() + " · Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        float adminLineWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(adminLine) / 1000 * 9;
        contentStream.beginText();
        contentStream.newLineAtOffset((page.getMediaBox().getWidth() - adminLineWidth) / 2, yPosition);
        contentStream.showText(adminLine);
        contentStream.endText();
        yPosition -= 20;

        // --- Ventes de Produits ---
        if (!report.getPayments().isEmpty()) {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Ventes de Produits");
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);
            contentStream.setStrokingColor(0.85f, 0.85f, 0.85f);
            contentStream.moveTo(margin, yPosition - 4);
            contentStream.lineTo(margin + 120, yPosition - 4);
            contentStream.stroke();
            contentStream.setStrokingColor(0f, 0f, 0f);
            yPosition -= leading;

            // Tableau des ventes de produits
            float[] paymentColumnWidths = {0.20f, 0.20f, 0.15f, 0.15f, 0.20f};
            yPosition = drawTable(
                document, contentStream, page, yPosition, margin, paymentColumnWidths,
                new String[]{"N° Ticket", "Client", "Montant", "Mode Paiement", "Heure"},
                report.getPayments(),
                (itemObj, cs, startX, startY, columnWidths) -> {
                    Payment p = (Payment) itemObj;
                    cs.showText(p.getNumeroTicket());
                    cs.newLineAtOffset(columnWidths[0], 0);
                    cs.showText(p.getClient() != null ? p.getClient().getName() : "N/A");
                    cs.newLineAtOffset(columnWidths[1], 0);
                    cs.showText(String.format("%.2f", p.getMontantTotal()));
                    cs.newLineAtOffset(columnWidths[2], 0);
                    cs.showText(p.getModePaiement());
                    cs.newLineAtOffset(columnWidths[3], 0);
                    cs.showText(p.getDateHeure().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
            );

            // Ligne de séparation avant le total
            contentStream.setStrokingColor(0.7f, 0.7f, 0.7f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            contentStream.setStrokingColor(0f, 0f, 0f);
            yPosition -= 15; // Espace supplémentaire avant le total

            // Total des ventes de produits (centré et espacé)
            double totalVentes = report.getPayments().stream().mapToDouble(Payment::getMontantTotal).sum();
            String totalVentesText = String.format("Total Ventes Produits : %.2f F", totalVentes);
            float totalVentesWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(totalVentesText) / 1000 * 12;
            float totalVentesX = (page.getMediaBox().getWidth() - totalVentesWidth) / 2;

            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.setNonStrokingColor(PDF_COLOR_SUCCESS_R, PDF_COLOR_SUCCESS_G, PDF_COLOR_SUCCESS_B);
            contentStream.beginText();
            contentStream.newLineAtOffset(totalVentesX, yPosition);
            contentStream.showText(totalVentesText);
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);
            yPosition -= leading * 3;
        }

        // --- Réservations ---
        if (!report.getReservations().isEmpty()) {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Réservations");
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);
            contentStream.setStrokingColor(0.85f, 0.85f, 0.85f);
            contentStream.moveTo(margin, yPosition - 4);
            contentStream.lineTo(margin + 90, yPosition - 4);
            contentStream.stroke();
            contentStream.setStrokingColor(0f, 0f, 0f);
            yPosition -= leading;

            // Tableau des réservations
            float[] reservationColumnWidths = {0.20f, 0.20f, 0.15f, 0.15f, 0.20f};
            yPosition = drawTable(
                document, contentStream, page, yPosition, margin, reservationColumnWidths,
                new String[]{"N° Ticket", "Client", "Montant", "Poste", "Heure"},
                report.getReservations(),
                (itemObj, cs, startX, startY, columnWidths) -> {
                    Reservation r = (Reservation) itemObj;
                    cs.showText(r.getNumeroTicket());
                    cs.newLineAtOffset(columnWidths[0], 0);
                    cs.showText(r.getClient() != null ? r.getClient().getName() : "N/A");
                    cs.newLineAtOffset(columnWidths[1], 0);
                    cs.showText(String.format("%.2f", r.getTotalPrice()));
                    cs.newLineAtOffset(columnWidths[2], 0);
                    cs.showText(r.getPoste() != null ? r.getPoste().getName() : "N/A");
                    cs.newLineAtOffset(columnWidths[3], 0);
                    cs.showText(r.getReservationDate().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
            );

            // Ligne de séparation avant le total
            contentStream.setStrokingColor(0.7f, 0.7f, 0.7f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            contentStream.setStrokingColor(0f, 0f, 0f);
            yPosition -= 15; // Espace supplémentaire avant le total

            // Total des réservations (centré et espacé)
            double totalReservations = report.getReservations().stream().mapToDouble(Reservation::getTotalPrice).sum();
            String totalReservationsText = String.format("Total Réservations : %.2f F", totalReservations);
            float totalReservationsWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(totalReservationsText) / 1000 * 12;
            float totalReservationsX = (page.getMediaBox().getWidth() - totalReservationsWidth) / 2;

            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.setNonStrokingColor(PDF_COLOR_SUCCESS_R, PDF_COLOR_SUCCESS_G, PDF_COLOR_SUCCESS_B);
            contentStream.beginText();
            contentStream.newLineAtOffset(totalReservationsX, yPosition);
            contentStream.showText(totalReservationsText);
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);
            yPosition -= leading * 3;
        }

        yPosition -= 12;

        // --- Grand Total (encadré) ---
        double totalVentes = report.getPayments().stream().mapToDouble(Payment::getMontantTotal).sum();
        double totalReservations = report.getReservations().stream().mapToDouble(Reservation::getTotalPrice).sum();
        double grandTotal = totalVentes + totalReservations;
        String grandTotalText = String.format("Grand Total Journalier : %.2f F", grandTotal);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        float grandTotalWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(grandTotalText) / 1000 * 16;
        float grandTotalX = (page.getMediaBox().getWidth() - grandTotalWidth) / 2;
        float boxPad = 12;
        contentStream.setNonStrokingColor(0.94f, 0.95f, 0.97f);
        contentStream.addRect(grandTotalX - boxPad, yPosition - 22, grandTotalWidth + 2 * boxPad, 28);
        contentStream.fill();
        contentStream.setStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
        contentStream.setLineWidth(1.2f);
        contentStream.addRect(grandTotalX - boxPad, yPosition - 22, grandTotalWidth + 2 * boxPad, 28);
        contentStream.stroke();
        contentStream.setStrokingColor(0f, 0f, 0f);
        contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
        contentStream.beginText();
        contentStream.newLineAtOffset(grandTotalX, yPosition - 16);
        contentStream.showText(grandTotalText);
        contentStream.endText();
        contentStream.setNonStrokingColor(0f, 0f, 0f);

        // Fermeture et sauvegarde
        contentStream.close();
        document.save(file);

        if (saveToFile) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport PDF généré avec succès !");
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    } catch (IOException e) {
        showAlert(Alert.AlertType.ERROR, "Erreur PDF", "Erreur lors de la génération du PDF: " + e.getMessage());
        e.printStackTrace();
    }
}


   @FXML
private void generateSelectedJournalPdf() {
    if (isAdmin) return;

    ObservableList<Object> selectedItems = journalTable.getSelectionModel().getSelectedItems();
    if (selectedItems.isEmpty()) {
        showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner au moins une ligne à inclure dans le rapport PDF.");
        return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Enregistrer le rapport sélectionné");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
    fileChooser.setInitialFileName("Rapport_Selection_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".pdf");
    File file = fileChooser.showSaveDialog(new Stage());

    if (file != null) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            float leading = 25;

            // --- Bandeau d'en-tête ---
            float headerHeight = 52;
            contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
            contentStream.addRect(0, yStart - headerHeight, page.getMediaBox().getWidth(), headerHeight);
            contentStream.fill();
            contentStream.setNonStrokingColor(1f, 1f, 1f);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            String headerTitle = "KAY PLAY GAMING ROOM";
            float headerTitleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(headerTitle) / 1000 * 20;
            contentStream.beginText();
            contentStream.newLineAtOffset((page.getMediaBox().getWidth() - headerTitleWidth) / 2, yStart - headerHeight + 18);
            contentStream.showText(headerTitle);
            contentStream.endText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            String headerSub = "Rapport sélectionné · " + selectedItems.size() + " jour(s)";
            float headerSubWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(headerSub) / 1000 * 10;
            contentStream.beginText();
            contentStream.newLineAtOffset((page.getMediaBox().getWidth() - headerSubWidth) / 2, yStart - headerHeight + 4);
            contentStream.showText(headerSub);
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);
            yPosition -= headerHeight + 20;

            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
            String genLine = "Généré par : " + currentUser.getName() + " · " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            float genLineWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(genLine) / 1000 * 9;
            contentStream.beginText();
            contentStream.newLineAtOffset((page.getMediaBox().getWidth() - genLineWidth) / 2, yPosition);
            contentStream.showText(genLine);
            contentStream.endText();
            yPosition -= 20;

            // --- Tableau des rapports sélectionnés ---
            float[] columnWidths = {0.25f, 0.25f, 0.25f, 0.25f};
            yPosition = drawTable(
                document, contentStream, page, yPosition, margin, columnWidths,
                new String[]{"Admin", "Date", "Total Ventes", "Total Réservations"},
                selectedItems,
                (item, cs, startX, startY, colWidths) -> {
                    AdminDailyReport report = (AdminDailyReport) item;
                    cs.showText(report.getAdmin().getName());
                    cs.newLineAtOffset(colWidths[0], 0);
                    cs.showText(report.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    cs.newLineAtOffset(colWidths[1], 0);
                    cs.showText(String.format("%.2f F", report.getPayments().stream().mapToDouble(Payment::getMontantTotal).sum()));
                    cs.newLineAtOffset(colWidths[2], 0);
                    cs.showText(String.format("%.2f F", report.getReservations().stream().mapToDouble(Reservation::getTotalPrice).sum()));
                }
            );

            // --- Calcul des totaux ---
            double totalVentes = selectedItems.stream()
                .mapToDouble(item -> ((AdminDailyReport) item).getPayments().stream().mapToDouble(Payment::getMontantTotal).sum())
                .sum();

            double totalReservations = selectedItems.stream()
                .mapToDouble(item -> ((AdminDailyReport) item).getReservations().stream().mapToDouble(Reservation::getTotalPrice).sum())
                .sum();

            double totalGlobal = totalVentes + totalReservations;

            // Ligne de séparation avant les totaux
            contentStream.setStrokingColor(0.7f, 0.7f, 0.7f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            contentStream.setStrokingColor(0f, 0f, 0f);
            yPosition -= 30; // Espace supplémentaire avant les totaux

            // --- Totaux (centrés et espacés) ---
            // Total Global Ventes
            String totalVentesText = String.format("Total Ventes Produits : %.2f F", totalVentes);
            float totalVentesWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(totalVentesText) / 1000 * 12;
            float totalVentesX = (page.getMediaBox().getWidth() - totalVentesWidth) / 2;

            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.setNonStrokingColor(PDF_COLOR_SUCCESS_R, PDF_COLOR_SUCCESS_G, PDF_COLOR_SUCCESS_B);
            contentStream.beginText();
            contentStream.newLineAtOffset(totalVentesX, yPosition);
            contentStream.showText(totalVentesText);
            contentStream.endText();
            yPosition -= leading * 2;

            String totalReservationsText = String.format("Total Réservations : %.2f F", totalReservations);
            float totalReservationsWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(totalReservationsText) / 1000 * 12;
            float totalReservationsX = (page.getMediaBox().getWidth() - totalReservationsWidth) / 2;
            contentStream.beginText();
            contentStream.newLineAtOffset(totalReservationsX, yPosition);
            contentStream.showText(totalReservationsText);
            contentStream.endText();
            yPosition -= leading * 2;

            yPosition -= 12;

            String grandTotalText = String.format("Total Global : %.2f F", totalGlobal);
            float grandTotalWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(grandTotalText) / 1000 * 14;
            float grandTotalX = (page.getMediaBox().getWidth() - grandTotalWidth) / 2;
            float boxPad = 12;
            contentStream.setNonStrokingColor(0.94f, 0.95f, 0.97f);
            contentStream.addRect(grandTotalX - boxPad, yPosition - 20, grandTotalWidth + 2 * boxPad, 26);
            contentStream.fill();
            contentStream.setStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
            contentStream.setLineWidth(1.2f);
            contentStream.addRect(grandTotalX - boxPad, yPosition - 20, grandTotalWidth + 2 * boxPad, 26);
            contentStream.stroke();
            contentStream.setStrokingColor(0f, 0f, 0f);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            contentStream.setNonStrokingColor(PDF_COLOR_PRIMARY_R, PDF_COLOR_PRIMARY_G, PDF_COLOR_PRIMARY_B);
            contentStream.beginText();
            contentStream.newLineAtOffset(grandTotalX, yPosition - 14);
            contentStream.showText(grandTotalText);
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);

            // Fermeture et sauvegarde
            contentStream.close();
            document.save(file);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport PDF généré avec succès !");
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur PDF", "Impossible de générer le rapport PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }
}


    @FunctionalInterface
    interface PDFRowDrawer {
        void draw(Object item, PDPageContentStream contentStream, float startX, float startY, float[] columnWidths) throws IOException;
    }

    private float drawTable(
        PDDocument document,
        PDPageContentStream contentStream,
        PDPage page,
        float yPosition,
        float margin,
        float[] columnWidths,
        String[] headers,
        List<?> dataList,
        PDFRowDrawer rowDrawer
    ) throws IOException {
        float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
        float rowHeight = 20f;
        float cellMargin = 5f;
        float[] actualColumnWidths = new float[columnWidths.length];
        for (int i = 0; i < columnWidths.length; i++) {
            actualColumnWidths[i] = columnWidths[i] * tableWidth;
        }

        // En-tête du tableau (bandeau bleu, texte blanc)
        contentStream.setNonStrokingColor(PDF_COLOR_HEADER_BG_R, PDF_COLOR_HEADER_BG_G, PDF_COLOR_HEADER_BG_B);
        contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(1f, 1f, 1f);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        float currentX = margin;
        for (int i = 0; i < headers.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(currentX + cellMargin, yPosition - rowHeight + 5);
            contentStream.showText(headers[i]);
            contentStream.endText();
            currentX += actualColumnWidths[i];
        }
        contentStream.setNonStrokingColor(0f, 0f, 0f);
        yPosition -= rowHeight;
        contentStream.moveTo(margin, yPosition);
        contentStream.lineTo(margin + tableWidth, yPosition);
        contentStream.stroke();

        // Lignes du tableau
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
        for (Object item : dataList) {
            if (yPosition < margin + rowHeight) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                yPosition = page.getMediaBox().getHeight() - margin;
                contentStream.setNonStrokingColor(PDF_COLOR_HEADER_BG_R, PDF_COLOR_HEADER_BG_G, PDF_COLOR_HEADER_BG_B);
                contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                contentStream.fill();
                contentStream.setNonStrokingColor(1f, 1f, 1f);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                currentX = margin;
                for (int i = 0; i < headers.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(currentX + cellMargin, yPosition - rowHeight + 5);
                    contentStream.showText(headers[i]);
                    contentStream.endText();
                    currentX += actualColumnWidths[i];
                }
                contentStream.setNonStrokingColor(0f, 0f, 0f);
                yPosition -= rowHeight;
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(margin + tableWidth, yPosition);
                contentStream.stroke();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
            }

            // Dessine les bordures verticales
            currentX = margin;
            for (int i = 0; i < actualColumnWidths.length; i++) {
                contentStream.moveTo(currentX, yPosition);
                contentStream.lineTo(currentX, yPosition + rowHeight);
                contentStream.stroke();
                currentX += actualColumnWidths[i];
            }
            contentStream.moveTo(currentX, yPosition);
            contentStream.lineTo(currentX, yPosition + rowHeight);
            contentStream.stroke();

            // Contenu de la ligne
            contentStream.beginText();
            contentStream.newLineAtOffset(margin + cellMargin, yPosition - rowHeight + 5);
            rowDrawer.draw(item, contentStream, margin + cellMargin, yPosition - rowHeight + 5, actualColumnWidths);
            contentStream.endText();

            // Ligne horizontale en bas de la ligne
            yPosition -= rowHeight;
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(margin + tableWidth, yPosition);
            contentStream.stroke();
        }
        return yPosition;
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
