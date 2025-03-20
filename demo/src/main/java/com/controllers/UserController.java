package com.controllers;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.Parrain;
import com.entities.Role;
import com.entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class UserController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> nameUserColumn;
    @FXML
    private TableColumn<User, String> emailUserColumn;
    @FXML
    private TableColumn<User, String> roleUserColumn;
    @FXML
    private Button addAdminButton, editAdminButton, deleteAdminButton;
    
    @FXML
    private TextField searchClientField, searchAdminField, searchParrainField;
    
    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, String> nameClientColumn, phoneClientColumn, addressClientColumn;
    @FXML
    private TableColumn<Client, Integer> loyaltyClientColumn;
    @FXML
    private Button  editClientButton, deleteClientButton;

    @FXML
    private TableView<Parrain> parrainTable;
    @FXML
    private TableColumn<Parrain, String> nameParrainColumn;
    @FXML
    private TableColumn<Parrain, String> emailParrainColumn;
    @FXML
    private TableColumn<Parrain, String> phoneParrainColumn;
    @FXML
    private TableColumn<Parrain, String> addressParrainColumn;
    @FXML
    private TableColumn<Parrain, String> codeParrainageColumn;

    @FXML
    private Button addParrainButton, editParrainButton, deleteParrainButton;

    @FXML
    private Pagination paginationClient, paginationAdmin, paginationParrain;
    private static final int PAGE_SIZE = 25; // Nombre d'éléments par page

    private ObservableList<User> users = FXCollections.observableArrayList(Fabrique.getService().findAllUsers());
    private ObservableList<Client> clients = FXCollections.observableArrayList(Fabrique.getService().getAllClients());
    private ObservableList<Parrain> parrains = FXCollections.observableArrayList(Fabrique.getService().getAllParrains());

    @FXML
    public void initialize() {
        setupTables();
        setupPagination();
        searchClientField.textProperty().addListener((obs, oldVal, newVal) -> 
            filterList(newVal, clients, clientTable, paginationClient));
        searchAdminField.textProperty().addListener((obs, oldVal, newVal) -> 
            filterList(newVal, users, userTable, paginationAdmin));
        searchParrainField.textProperty().addListener((obs, oldVal, newVal) -> 
            filterList(newVal, parrains, parrainTable, paginationParrain));
        loadUsers();
         // Masquer les boutons si l'utilisateur n'est pas un SuperAdmin
        if (ConnexionController.user == null || !ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
            addAdminButton.setVisible(false); // Masquer le bouton "Ajouter un utilisateur"
            editAdminButton.setVisible(false); // Masquer le bouton "Modifier un utilisateur"
            deleteAdminButton.setVisible(false); // Masquer le bouton "Supprimer un utilisateur"
        }
    }

    private void setupTables() {
        nameUserColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailUserColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleUserColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        nameClientColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneClientColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressClientColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        loyaltyClientColumn.setCellValueFactory(new PropertyValueFactory<>("loyaltyPoints"));

        nameParrainColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailParrainColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneParrainColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressParrainColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        codeParrainageColumn.setCellValueFactory(new PropertyValueFactory<>("codeParrainage"));
    }

    private void setupPagination() {
        paginationClient.setPageFactory(this::createPageClient);
        paginationAdmin.setPageFactory(this::createPageUser);
        paginationParrain.setPageFactory(this::createPageParrain);
    
        // Initialiser le nombre de pages
        updatePagination(paginationClient, clients.size());
        updatePagination(paginationAdmin, users.size());
        updatePagination(paginationParrain, parrains.size());
    }
    private int getPageCount(List<?> list) {
        return (int) Math.ceil((double) list.size() / PAGE_SIZE);
    }

    private ObservableList<User> getUsersPage(int pageIndex) {
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, users.size());
        return FXCollections.observableArrayList(users.subList(fromIndex, toIndex));
    }

    private ObservableList<Client> getClientsPage(int pageIndex) {
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, clients.size());
        return FXCollections.observableArrayList(clients.subList(fromIndex, toIndex));
    }

    private ObservableList<Parrain> getParrainsPage(int pageIndex) {
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, parrains.size());
        return FXCollections.observableArrayList(parrains.subList(fromIndex, toIndex));
    }

    private void updatePagination(Pagination pagination, int totalItems) {
        int pageCount = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        pagination.setPageCount(pageCount);
    }

    private void loadUsers() {
        users.setAll(Fabrique.getService().findAllUsers());
        updatePagination(paginationAdmin, users.size()); // Mettre à jour la pagination des utilisateurs
        userTable.setItems(getUsersPage(0)); // Afficher la première page
    
        parrains.setAll(Fabrique.getService().getAllParrains());
        updatePagination(paginationParrain, parrains.size()); // Mettre à jour la pagination des parrains
        parrainTable.setItems(getParrainsPage(0)); // Afficher la première page
    
        List<Client> allClients = Fabrique.getService().getAllClients();
        List<Client> filteredClients = allClients.stream()
                .filter(client -> client.getTotalPlayTime().toHours() >= 5)
                .collect(Collectors.toList());
        clients.setAll(filteredClients);
        updatePagination(paginationClient, clients.size()); // Mettre à jour la pagination des clients
        clientTable.setItems(getClientsPage(0)); // Afficher la première page
    }

    private <T> void filterList(String query, ObservableList<T> list, TableView<T> table, Pagination pagination) {
        List<T> filteredList = list.stream()
                .filter(item -> {
                    if (item instanceof Client) {
                        Client client = (Client) item;
                        return client.getName().toLowerCase().contains(query.toLowerCase()) &&
                               client.getTotalPlayTime().toHours() >= 5;
                    } else if (item instanceof Parrain) {
                        return ((Parrain) item).getName().toLowerCase().contains(query.toLowerCase());
                    } else if (item instanceof User) {
                        return ((User) item).getName().toLowerCase().contains(query.toLowerCase());
                    }
                    return false;
                })
                .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(filteredList));
        updatePagination(pagination, filteredList.size()); // Mettre à jour la pagination après filtrage
    }

    private Node createPageClient(int pageIndex) {
        clientTable.setItems(getClientsPage(pageIndex));
        return clientTable;
    }

    private Node createPageUser(int pageIndex) {
        userTable.setItems(getUsersPage(pageIndex));
        return userTable;
    }

    private Node createPageParrain(int pageIndex) {
        parrainTable.setItems(getParrainsPage(pageIndex));
        return parrainTable;
    }

    private void openModalWindow(String fxmlPath, String title, Object entity) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            if (entity != null) {
                Object controller = loader.getController();
                if (controller instanceof EditUserController) {
                    ((EditUserController) controller).setUser((User) entity);
                } else if (controller instanceof EditClientController) {
                    ((EditClientController) controller).setClient((Client) entity);
                }
            }

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur lors de l'ouverture de la fenêtre : " + e.getMessage());
        }
    }

    @FXML
    private void openAddUserWindow() {
        // Vérifier si l'utilisateur connecté est un SuperAdmin
        if (ConnexionController.user != null && ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
            openModalWindow("/com/views/AddUserWindow.fxml", "Ajouter un utilisateur", null);
        } else {
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires.");
        }
    }
    
    @FXML
    private void openEditUserWindow() {
        // Vérifier si l'utilisateur connecté est un SuperAdmin
        if (ConnexionController.user != null && ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                showAlert(AlertType.WARNING, "Veuillez sélectionner un utilisateur à modifier.");
                return;
            }
            openModalWindow("/com/views/EditUserWindow.fxml", "Modifier un utilisateur", selectedUser);
        } else {
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires.");
        }
    }

    @FXML
    private void openAddClientWindow() {
        openModalWindow("/com/views/AddClientWindow.fxml", "Ajouter un client", null);
    }

    @FXML
    private void openEditClientWindow() {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert(AlertType.WARNING, "Veuillez sélectionner un client à modifier.");
            return;
        }
        openModalWindow("/com/views/EditClientWindow.fxml", "Modifier un client", selectedClient);
    }

    @FXML
    private void openAddParrainWindow() {
        openModalWindow("/com/views/AddParrainWindow.fxml", "Ajouter un parrain", null);
    }

    @FXML
    private void openEditParrainWindow() {
        Parrain selectedParrain = parrainTable.getSelectionModel().getSelectedItem();
        if (selectedParrain == null) {
            showAlert(AlertType.WARNING, "Veuillez sélectionner un parrain à modifier.");
            return;
        }
        openModalWindow("/com/views/EditParrainWindow.fxml", "Modifier un parrain", selectedParrain);
    }

    @FXML
    private void deleteUser() {
        // Vérifier si l'utilisateur connecté est un SuperAdmin
        if (ConnexionController.user != null && ConnexionController.user.getRole().equals(Role.SuperAdmin)){
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                showAlert(AlertType.WARNING, "Veuillez sélectionner un utilisateur à supprimer.");
                return;
            }
        
            Alert confirm = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cet utilisateur ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        Fabrique.getService().deleteUser(selectedUser);
                        users.remove(selectedUser);
                        updatePagination(paginationAdmin, users.size()); // Mettre à jour la pagination
                        userTable.setItems(getUsersPage(paginationAdmin.getCurrentPageIndex())); // Rafraîchir la page actuelle
                        showAlert(AlertType.INFORMATION, "Utilisateur supprimé avec succès.");
                    } catch (Exception e) {
                        showAlert(AlertType.ERROR, "Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
                    }
                }
            });
        }else{
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires.");
        }
       
    }
    
    @FXML
    private void deleteParrain() {
        Parrain selectedParrain = parrainTable.getSelectionModel().getSelectedItem();
        if (selectedParrain == null) {
            showAlert(AlertType.WARNING, "Veuillez sélectionner un parrain à supprimer.");
            return;
        }
    
        Alert confirm = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce parrain ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    Fabrique.getService().deleteParrain(selectedParrain);
                    parrains.remove(selectedParrain);
                    updatePagination(paginationParrain, parrains.size()); // Mettre à jour la pagination
                    parrainTable.setItems(getParrainsPage(paginationParrain.getCurrentPageIndex())); // Rafraîchir la page actuelle
                    showAlert(AlertType.INFORMATION, "Parrain supprimé avec succès.");
                } catch (Exception e) {
                    showAlert(AlertType.ERROR, "Erreur lors de la suppression du parrain : " + e.getMessage());
                }
            }
        });
    }

    private boolean isSuperAdmin(User user) {
        return user != null && user.getRole().equals(Role.SuperAdmin);
    }
    
    private void showAlert(AlertType type, String message) {
        new Alert(type, message).show();
    }
}
