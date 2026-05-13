package com.controllers;

import com.core.Fabrique;
import com.entities.Categorie;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class CategoriesManagerController implements Initializable {

    @FXML private TextField nomField;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private TableView<Categorie> categoriesTable;
    @FXML private TableColumn<Categorie, Integer> colId;
    @FXML private TableColumn<Categorie, String> colNom;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        loadCategories();
        categoriesTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                nomField.setText(n.getNom());
            }
        });
    }

    private void loadCategories() {
        categoriesTable.getItems().setAll(Fabrique.getService().getAllCategories());
    }

    @FXML
    private void addCategorie() {
        String nom = nomField.getText() != null ? nomField.getText().trim() : "";
        if (nom.isEmpty()) {
            ControllerUtils.showErrorAlert("Champ vide", "Veuillez saisir un nom de catégorie.");
            return;
        }
        try {
            Categorie c = new Categorie(nom);
            Fabrique.getService().addCategorie(c);
            ControllerUtils.showInfoAlert("Succès", "Catégorie ajoutée.");
            nomField.clear();
            loadCategories();
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void editCategorie() {
        Categorie selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ControllerUtils.showErrorAlert("Aucune sélection", "Sélectionnez une catégorie à modifier.");
            return;
        }
        String nom = nomField.getText() != null ? nomField.getText().trim() : "";
        if (nom.isEmpty()) {
            ControllerUtils.showErrorAlert("Champ vide", "Veuillez saisir un nom.");
            return;
        }
        try {
            selected.setNom(nom);
            Fabrique.getService().updateCategorie(selected);
            ControllerUtils.showInfoAlert("Succès", "Catégorie modifiée.");
            loadCategories();
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void deleteCategorie() {
        Categorie selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ControllerUtils.showErrorAlert("Aucune sélection", "Sélectionnez une catégorie à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer la catégorie \"" + selected.getNom() + "\" ? Les produits de cette catégorie ne seront pas supprimés.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            Fabrique.getService().deleteCategorie(selected);
            ControllerUtils.showInfoAlert("Succès", "Catégorie supprimée.");
            nomField.clear();
            loadCategories();
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur", e.getMessage());
        }
    }
}
