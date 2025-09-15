package com.controllers;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Client;
import com.entities.Parrain;
import com.entities.Role;
import com.entities.User;
import com.entities.Reservation; // Not directly used in this controller for now, but kept for context
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class UserController {

    // Élément FXML pour la gestion des administrateurs (Admins)
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
    private TextField searchAdminField;
    @FXML
    private Pagination paginationAdmin;

    // Élément FXML pour la gestion des clients (Clients)
    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, String> nameClientColumn, phoneClientColumn, addressClientColumn;
    @FXML
    private TableColumn<Client, Integer> loyaltyClientColumn;
    @FXML
    private Button editClientButton, deleteClientButton;
    @FXML
    private TextField searchClientField;
    @FXML
    private DatePicker filterClientDatePicker;
    @FXML
    private Button searchClientButton, resetDateFilterButton; // searchClientButton might be redundant with text listener
    @FXML
    private Pagination paginationClient;

    // Élément FXML pour la gestion des parrains (Parrains)
    @FXML
    private TableView<Parrain> parrainTable;
    @FXML
    private TableColumn<Parrain, String> nameParrainColumn;
    @FXML
    private TableColumn<Parrain, Integer> pointsParrainageColumn;
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
    private TextField searchParrainField;
    @FXML
    private Pagination paginationParrain;

    private static final int PAGE_SIZE = 25;

    // Listes de toutes les données (non filtrées)
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private ObservableList<Client> allClients = FXCollections.observableArrayList();
    private ObservableList<Parrain> allParrains = FXCollections.observableArrayList();

    // Listes des données actuellement affichées (filtrées et paginées)
    private ObservableList<User> displayedUsers = FXCollections.observableArrayList();
    private ObservableList<Client> displayedClients = FXCollections.observableArrayList();
    private ObservableList<Parrain> displayedParrains = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Initialiser les colonnes des TableView
        setupTables();
        setupColumnResizePolicies();

        // 2. Charger toutes les données depuis la fabrique (base de données/service)
        refreshAllData(); // Use this method to initially load and filter

        // 3. Configurer les écouteurs de texte pour les champs de recherche
        searchClientField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchAdminField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchParrainField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // 4. Configurer l'écouteur pour le DatePicker des clients
        filterClientDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> applyFilters());

        // 5. Mettre en place la gestion des permissions pour les boutons
        managePermissions();

        // 6. Configurer la pagination pour chaque TableView
        setupPagination();
    }

    /**
     * Configure les CellValueFactory pour toutes les colonnes des TableView.
     */
    private void setupTables() {
        // Colonnes Admin
        nameUserColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailUserColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleUserColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userTable.setItems(displayedUsers); // Lie le TableView à la liste des utilisateurs affichés

        // Colonnes Client
        nameClientColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneClientColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressClientColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        loyaltyClientColumn.setCellValueFactory(new PropertyValueFactory<>("loyaltyPoints"));
        clientTable.setItems(displayedClients); // Lie le TableView à la liste des clients affichés

        // Colonnes Parrain
        nameParrainColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailParrainColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneParrainColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressParrainColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        codeParrainageColumn.setCellValueFactory(new PropertyValueFactory<>("codeParrainage"));
        pointsParrainageColumn.setCellValueFactory(new PropertyValueFactory<>("parrainagePoints"));
        parrainTable.setItems(displayedParrains); // Lie le TableView à la liste des parrains affichés
    }

    private void setupColumnResizePolicies() {
        // Utilisez la politique de redimensionnement moderne
        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        parrainTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    /**
     * Configure la pagination pour chaque TableView.
     */
    private void setupPagination() {
        paginationClient.setPageFactory(this::createPageClient);
        paginationAdmin.setPageFactory(this::createPageUser);
        paginationParrain.setPageFactory(this::createPageParrain);
    }

    /**
     * Met à jour le nombre total de pages pour une pagination donnée.
     * @param pagination L'objet Pagination à mettre à jour.
     * @param totalItems Le nombre total d'éléments.
     */
    private void updatePaginationPageCount(Pagination pagination, int totalItems) {
        int pageCount = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        pagination.setPageCount(Math.max(1, pageCount)); // Assure au moins 1 page
    }

    /**
     * Méthode générique pour créer une page pour la pagination.
     * @param pageIndex L'index de la page à créer.
     * @return Le Node (TableView) pour la page.
     */
    private Node createPageUser(int pageIndex) {
        userTable.setItems(getUsersPage(pageIndex));
        return userTable;
    }

    private Node createPageClient(int pageIndex) {
        clientTable.setItems(getClientsPage(pageIndex));
        return clientTable;
    }

    private Node createPageParrain(int pageIndex) {
        parrainTable.setItems(getParrainsPage(pageIndex));
        return parrainTable;
    }

    /**
     * Récupère une sous-liste d'utilisateurs pour la pagination.
     * @param pageIndex L'index de la page.
     * @return Une ObservableList d'utilisateurs pour la page.
     */
    private ObservableList<User> getUsersPage(int pageIndex) {
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, displayedUsers.size());
        return FXCollections.observableArrayList(displayedUsers.subList(fromIndex, toIndex));
    }

    /**
     * Récupère une sous-liste de clients pour la pagination.
     * @param pageIndex L'index de la page.
     * @return Une ObservableList de clients pour la page.
     */
    private ObservableList<Client> getClientsPage(int pageIndex) {
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, displayedClients.size());
        return FXCollections.observableArrayList(displayedClients.subList(fromIndex, toIndex));
    }

    /**
     * Récupère une sous-liste de parrains pour la pagination.
     * @param pageIndex L'index de la page.
     * @return Une ObservableList de parrains pour la page.
     */
    private ObservableList<Parrain> getParrainsPage(int pageIndex) {
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, displayedParrains.size());
        return FXCollections.observableArrayList(displayedParrains.subList(fromIndex, toIndex));
    }



    /**
     * Méthode principale pour appliquer tous les filtres à toutes les sections.
     * Cette méthode est appelée par les écouteurs des champs de recherche et du DatePicker.
     */
    @FXML
    private void applyFilters() {
        // Filtrer les clients
        String clientQuery = searchClientField.getText() == null ? "" : searchClientField.getText().toLowerCase();
        LocalDate reservationDateFilter = filterClientDatePicker.getValue();

        List<Client> filteredClientsList = allClients.stream()
                .filter(client -> {
                    boolean matchesName = client.getName().toLowerCase().contains(clientQuery);
                    boolean matchesReservationDate = true;

                    if (reservationDateFilter != null) {
                        // For clients, we check if ANY of their reservations match the date
                        // It's assumed Client.getReservations() loads associated reservations.
                        // If not, you might need to query the service for reservations by client.
                        matchesReservationDate = client.getReservations().stream()
                                .anyMatch(reservation -> {
                                    if (reservation.getReservationDate() != null) {
                                        LocalDate dateRes = reservation.getReservationDate().toLocalDate();
                                        return dateRes.equals(reservationDateFilter);
                                    }
                                    return false;
                                });
                    }

                    // *** CRITICAL UPDATE HERE ***
                    // We need to call the service to get the actual total play time,
                    // as the Client entity itself won't have this populated automatically
                    // from GameSession data without an explicit fetch or calculation.
                    Duration totalPlayTime = Fabrique.getService().getTotalPlayTime(client.getId());
                    boolean hasEnoughPlayTime = totalPlayTime != null && totalPlayTime.toHours() >= 50;

                    return matchesName && matchesReservationDate && hasEnoughPlayTime;
                })
                .collect(Collectors.toList());
        displayedClients.setAll(filteredClientsList);
        updatePaginationPageCount(paginationClient, displayedClients.size());
        paginationClient.setCurrentPageIndex(0);
        clientTable.setItems(getClientsPage(0));

        // Filtrer les administrateurs
        String adminQuery = searchAdminField.getText() == null ? "" : searchAdminField.getText().toLowerCase();
        List<User> filteredUsersList = allUsers.stream()
                .filter(user -> user.getName().toLowerCase().contains(adminQuery))
                .filter(user -> user.getRole().equals(Role.Admin)) // <-- Ajoutez cette ligne
                .collect(Collectors.toList());
        displayedUsers.setAll(filteredUsersList);
        updatePaginationPageCount(paginationAdmin, displayedUsers.size());
        paginationAdmin.setCurrentPageIndex(0);
        userTable.setItems(getUsersPage(0));

        // Filtrer les parrains
        String parrainQuery = searchParrainField.getText() == null ? "" : searchParrainField.getText().toLowerCase();
        List<Parrain> filteredParrainsList = allParrains.stream()
                .filter(parrain -> {
                    boolean matchesName = parrain.getName().toLowerCase().contains(parrainQuery);
                    boolean matchesCode = false;
                    if (parrain.getCodeParrainage() != null) {
                        matchesCode = parrain.getCodeParrainage().toLowerCase().contains(parrainQuery);
                    }
                    return matchesName || matchesCode;
                })
                .collect(Collectors.toList());
        displayedParrains.setAll(filteredParrainsList);
        // Important: Appliquer le tri par points de parrainage après le filtrage
        displayedParrains.sort((p1, p2) -> Integer.compare(p2.getParrainagePoints(), p1.getParrainagePoints()));
        updatePaginationPageCount(paginationParrain, displayedParrains.size());
        paginationParrain.setCurrentPageIndex(0);
        parrainTable.setItems(getParrainsPage(0));
    }



    /**
     * Réinitialise le filtre de date pour les clients.
     */
    @FXML
    private void handleResetDateFilter() {
        filterClientDatePicker.setValue(null);
        applyFilters();
    }

    /**
     * Recharge toutes les listes de données depuis le service et réapplique les filtres.
     * Utile après une opération d'ajout, modification ou suppression.
     */
    private void refreshAllData() {
        allUsers.setAll(Fabrique.getService().findAllUsers());
        allClients.setAll(Fabrique.getService().getAllClients());
        allParrains.setAll(Fabrique.getService().getAllParrains());
        applyFilters(); // Apply filters and refresh tables with new data
    }

    // You can keep a specific refresh for clients if needed, but refreshAllData is more comprehensive.
    // private void refreshClients() {
    //     allClients.setAll(Fabrique.getService().getAllClients());
    //     applyFilters();
    // }

    /**
     * Gère l'affichage/masquage des boutons en fonction du rôle de l'utilisateur connecté.
     */
    private void managePermissions() {
        User currentUser = ConnexionController.user;
        boolean isSuperAdmin = currentUser != null && currentUser.getRole().equals(Role.SuperAdmin);
        boolean isAdmin = currentUser != null && currentUser.getRole().equals(Role.Admin);

        // Admin buttons
        if (addAdminButton != null) addAdminButton.setVisible(isSuperAdmin);
        if (editAdminButton != null) editAdminButton.setVisible(isSuperAdmin);
        if (deleteAdminButton != null) deleteAdminButton.setVisible(isSuperAdmin);

        // Client buttons
        if (editClientButton != null) editClientButton.setVisible(isSuperAdmin); // Seuls les SuperAdmin peuvent modifier
        if (deleteClientButton != null) deleteClientButton.setVisible(isSuperAdmin);

        // Sponsor (Parrain) buttons
        if (addParrainButton != null) addParrainButton.setVisible(isSuperAdmin || isAdmin);
        if (editParrainButton != null) editParrainButton.setVisible(isSuperAdmin); // Seuls les SuperAdmin peuvent modifier
        if (deleteParrainButton != null) deleteParrainButton.setVisible(isSuperAdmin);
    }


    private void openModalWindow(String fxmlPath, String title, Object entity) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            String viewName = fxmlPath.substring(fxmlPath.lastIndexOf("/") + 1, fxmlPath.lastIndexOf("."));
            WindowManager.closeWindowsForView(viewName);
            WindowManager.register(viewName, stage);

            if (entity != null) {
                Object controller = loader.getController();
                if (controller instanceof EditUserController) {
                    ((EditUserController) controller).setUser((User) entity);
                } else if (controller instanceof EditClientController) {
                    ((EditClientController) controller).setClient((Client) entity);
                } else if (controller instanceof EditParrainController) {
                    ((EditParrainController) controller).setParrainToEdit((Parrain) entity);
                }
            }

            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.setOnHidden(event -> refreshAllData()); // Refresh all data when modal closes

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur lors de l'ouverture de la fenêtre : " + e.getMessage());
        }
    }

    @FXML
    private void openAddUserWindow() {
        if (ConnexionController.user != null && ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
            openModalWindow("/com/views/AddUserWindow.fxml", "Ajouter un utilisateur", null);
        } else {
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires.");
        }
    }

    @FXML
    private void openEditUserWindow() {
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
        // Permission check might be needed here based on your design
        openModalWindow("/com/views/AddClientWindow.fxml", "Ajouter un client", null);
    }

   @FXML
    private void openEditClientWindow() {
        // 1. Vérifie d'abord si l'utilisateur est un SuperAdmin
        if (ConnexionController.user == null || !ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires pour modifier un client.");
            return; // Arrête l'exécution si l'utilisateur n'a pas les droits
        }

        // 2. Vérifie qu'un client est sélectionné
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert(AlertType.WARNING, "Veuillez sélectionner un client à modifier.");
            return;
        }

        // 3. Ouvre la fenêtre de modification
        openModalWindow("/com/views/EditClientWindow.fxml", "Modifier un client", selectedClient);
    }


    @FXML
    private void openAddParrainWindow() {
        // Permission check might be needed here
        openModalWindow("/com/views/AddParrainWindow.fxml", "Ajouter un parrain", null);
    }

    @FXML
    private void openEditParrainWindow() {
        // 1. Vérifie les permissions
        if (ConnexionController.user == null || !ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires pour modifier un parrain.");
            return;
        }

        // 2. Vérifie la sélection
        Parrain selectedParrain = parrainTable.getSelectionModel().getSelectedItem();
        if (selectedParrain == null) {
            showAlert(AlertType.WARNING, "Veuillez sélectionner un parrain à modifier.");
            return;
        }

        // 3. Ouvre la fenêtre
        openModalWindow("/com/views/EditParrainWindow.fxml", "Modifier un parrain", selectedParrain);
    }


    @FXML
    private void deleteUser() {
        if (ConnexionController.user != null && ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
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
                        showAlert(AlertType.INFORMATION, "Utilisateur supprimé avec succès.");
                        refreshAllData();
                    } catch (Exception e) {
                        showAlert(AlertType.ERROR, "Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
                    }
                }
            });
        } else {
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires.");
        }
    }

    @FXML
    private void deleteClient() {
        if (ConnexionController.user != null && ConnexionController.user.getRole().equals(Role.SuperAdmin)) { // Only SuperAdmin can delete
            Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
            if (selectedClient == null) {
                showAlert(AlertType.WARNING, "Veuillez sélectionner un client à supprimer.");
                return;
            }
            Alert confirm = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce client ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        Fabrique.getService().deleteClient(selectedClient);
                        showAlert(AlertType.INFORMATION, "Client supprimé avec succès.");
                        refreshAllData();
                    } catch (Exception e) {
                        showAlert(AlertType.ERROR, "Erreur lors de la suppression du client : " + e.getMessage());
                    }
                }
            });
        } else {
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires pour supprimer un client.");
        }
    }

    @FXML
    private void deleteParrain() {
        if (ConnexionController.user != null && ConnexionController.user.getRole().equals(Role.SuperAdmin)) { // Only SuperAdmin can delete
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
                        showAlert(AlertType.INFORMATION, "Parrain supprimé avec succès.");
                        refreshAllData();
                    } catch (Exception e) {
                        showAlert(AlertType.ERROR, "Erreur lors de la suppression du parrain : " + e.getMessage());
                    }
                }
            });
        } else {
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires pour supprimer un parrain.");
        }
    }

    private void showAlert(AlertType type, String message) {
        new Alert(type, message).show();
    }
}