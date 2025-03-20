package com.controllers;

import com.entities.Payment;
import com.entities.Produit;
import com.entities.Reservation;
import com.core.Fabrique;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

public class FinanceController {

    @FXML
    private TableView<Object> entreesTable;
    @FXML
    private TableColumn<Object, String> numeroTicketColumn;
    @FXML
    private TableColumn<Object, String> clientColumn;
    @FXML
    private TableColumn<Object, String> montantTotalColumn;
    @FXML
    private TableColumn<Object, String> modePaiementColumn;
    @FXML
    private TableColumn<Object, String> dateHeureColumn;

    @FXML
    private TableView<Produit> sortiesTable;
    @FXML
    private TableColumn<Produit, String> nomProduitColumn;
    @FXML
    private TableColumn<Produit, Double> prixProduitColumn;
    @FXML
    private TableColumn<Produit, Integer> stockProduitColumn;
    @FXML
    private TableColumn<Produit, String> dateAjoutColumn;

    private ObservableList<Object> entreesList;
    private ObservableList<Produit> sortiesList;

    @FXML
    private TextField searchEntreeField;
    @FXML
    private TextField searchSortieField;

    @FXML
    private Pagination entreesPagination;
    @FXML
    private Pagination sortiesPagination;

    private static final int ITEMS_PER_PAGE = 25; // Nombre d'éléments par page

    @FXML
    public void initialize() {
        // Configurer les colonnes pour les entrées (paiements et réservations)
        numeroTicketColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Payment) {
                return new SimpleStringProperty(((Payment) cellData.getValue()).getNumeroTicket());
            } else if (cellData.getValue() instanceof Reservation) {
                return new SimpleStringProperty(((Reservation) cellData.getValue()).getNumeroTicket());
            }
            return new SimpleStringProperty("");
        });

        clientColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Payment) {
                Payment payment = (Payment) cellData.getValue();
                return new SimpleStringProperty(payment.getClient() != null ? payment.getClient().getName() : "N/A");
            } else if (cellData.getValue() instanceof Reservation) {
                Reservation reservation = (Reservation) cellData.getValue();
                return new SimpleStringProperty(reservation.getClient() != null ? reservation.getClient().getName() : "N/A");
            }
            return new SimpleStringProperty("");
        });

        montantTotalColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Payment) {
                return new SimpleStringProperty(String.valueOf(((Payment) cellData.getValue()).getMontantTotal()));
            } else if (cellData.getValue() instanceof Reservation) {
                return new SimpleStringProperty("Réservation");
            }
            return new SimpleStringProperty("");
        });

        modePaiementColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Payment) {
                return new SimpleStringProperty(((Payment) cellData.getValue()).getModePaiement());
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        dateHeureColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Payment) {
                return new SimpleStringProperty(((Payment) cellData.getValue()).getDateHeure().toString());
            } else if (cellData.getValue() instanceof Reservation) {
                return new SimpleStringProperty(((Reservation) cellData.getValue()).getReservationDate().toString());
            }
            return new SimpleStringProperty("");
        });

        // Configurer les colonnes pour les sorties (produits)
        nomProduitColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prixProduitColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        stockProduitColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        dateAjoutColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateAjout().toString()));

        // Charger les données initiales
        loadEntrees();
        loadSorties();

        // Ajouter des gestionnaires d'événements pour la recherche
        searchEntreeField.textProperty().addListener((observable, oldValue, newValue) -> filterEntrees(newValue));
        searchSortieField.textProperty().addListener((observable, oldValue, newValue) -> filterSorties(newValue));
    }

    private void loadEntrees() {
        // Récupérer les paiements et les réservations depuis le service
        List<Payment> paiements = Fabrique.getService().getAllPayments();
        List<Reservation> reservations = Fabrique.getService().findAllReservations();
        
        entreesList = FXCollections.observableArrayList();
        entreesList.addAll(paiements);
        entreesList.addAll(reservations);
        
        entreesPagination.setPageCount((int) Math.ceil(entreesList.size() / (double) ITEMS_PER_PAGE));
        updateEntreesPagination(0); // Afficher la première page
    }

    private void loadSorties() {
        // Récupérer les produits depuis le service
        List<Produit> produits = Fabrique.getService().findAllProduits();
        sortiesList = FXCollections.observableArrayList(produits);
        sortiesPagination.setPageCount((int) Math.ceil(sortiesList.size() / (double) ITEMS_PER_PAGE));
        updateSortiesPagination(0); // Afficher la première page
    }

    private void filterEntrees(String searchText) {
        List<Object> filteredList = entreesList.stream()
                .filter(item -> item.toString().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());

        entreesTable.setItems(FXCollections.observableArrayList(filteredList));
        entreesPagination.setPageCount((int) Math.ceil(filteredList.size() / (double) ITEMS_PER_PAGE));
        updateEntreesPagination(0); // Mettre à jour la pagination
    }

    private void filterSorties(String searchText) {
        List<Produit> filteredList = sortiesList.stream()
                .filter(produit -> produit.getNom().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());

        sortiesTable.setItems(FXCollections.observableArrayList(filteredList));
        sortiesPagination.setPageCount((int) Math.ceil(filteredList.size() / (double) ITEMS_PER_PAGE));
        updateSortiesPagination(0); // Mettre à jour la pagination
    }

    private void updateEntreesPagination(int pageIndex) {
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, entreesList.size());
        List<Object> subList = entreesList.subList(startIndex, endIndex);
        entreesTable.setItems(FXCollections.observableArrayList(subList));
    }

    private void updateSortiesPagination(int pageIndex) {
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, sortiesList.size());
        List<Produit> subList = sortiesList.subList(startIndex, endIndex);
        sortiesTable.setItems(FXCollections.observableArrayList(subList));
    }

    @FXML
    private void handleEntreesPagination() {
        int pageIndex = entreesPagination.getCurrentPageIndex();
        updateEntreesPagination(pageIndex);
    }

    @FXML
    private void handleSortiesPagination() {
        int pageIndex = sortiesPagination.getCurrentPageIndex();
        updateSortiesPagination(pageIndex);
    }
}
