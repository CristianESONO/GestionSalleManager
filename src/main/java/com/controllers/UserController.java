package com.controllers;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Client;
import com.entities.Parrain;
import com.entities.Role;
import com.entities.User;
import com.entities.Reservation;
import com.entities.GameSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private Button searchClientButton, resetDateFilterButton;
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

    // Élément FXML pour la gestion des clients avec temps restant
    @FXML
    private TableView<Client> remainingTimeClientsTable;
    @FXML
    private TableColumn<Client, String> nameRemainingClientColumn;
    @FXML
    private TableColumn<Client, String> phoneRemainingClientColumn;
    @FXML
    private TableColumn<Client, String> remainingTimeColumn;
    @FXML
    private TableColumn<Client, Void> actionsRemainingTimeColumn;

    @FXML
    private Button addParrainButton, editParrainButton, deleteParrainButton;
    @FXML
    private TextField searchParrainField;
    @FXML
    private Pagination paginationParrain;

    // AJOUT: Éléments FXML pour la pagination et recherche des comptes client
    @FXML
    private TextField searchRemainingTimeField;
    @FXML
    private Pagination paginationRemainingTime;

    private static final int PAGE_SIZE = 25;

    // Listes de toutes les données (non filtrées)
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private ObservableList<Client> allClients = FXCollections.observableArrayList();
    private ObservableList<Parrain> allParrains = FXCollections.observableArrayList();
    
    // AJOUT: Liste pour les comptes client avec temps restant
    private ObservableList<Client> allRemainingTimeClients = FXCollections.observableArrayList();

    // Listes des données actuellement affichées (filtrées et paginées)
    private ObservableList<User> displayedUsers = FXCollections.observableArrayList();
    private ObservableList<Client> displayedClients = FXCollections.observableArrayList();
    private ObservableList<Parrain> displayedParrains = FXCollections.observableArrayList();
    
    // AJOUT: Liste pour les comptes client affichés
    private ObservableList<Client> displayedRemainingTimeClients = FXCollections.observableArrayList();

    private static UserController instance;

    public UserController() {
        instance = this;
    }

    public static UserController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        // 1. Initialiser les colonnes des TableView
        setupTables();
        setupColumnResizePolicies();
        setupRemainingTimeClientsTable();

        // 2. Charger toutes les données depuis la fabrique (base de données/service)
        refreshAllData();

        // 3. Configurer les écouteurs de texte pour les champs de recherche
        searchClientField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchAdminField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchParrainField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        // AJOUT: Écouteur pour la recherche des comptes client
        searchRemainingTimeField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // 4. Configurer l'écouteur pour le DatePicker des clients
        filterClientDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> applyFilters());

        // 5. Mettre en place la gestion des permissions pour les boutons
        managePermissions();

        // 6. Configurer la pagination pour chaque TableView
        setupPagination();

        // 7. Charger les clients avec temps restant
        loadClientsWithRemainingTime();
    }

    /**
     * Configure les CellValueFactory pour toutes les colonnes des TableView.
     */
    private void setupTables() {
        // Colonnes Admin
        nameUserColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailUserColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleUserColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userTable.setItems(displayedUsers);

        // Colonnes Client
        nameClientColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneClientColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressClientColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        loyaltyClientColumn.setCellValueFactory(new PropertyValueFactory<>("loyaltyPoints"));
        clientTable.setItems(displayedClients);

        // Colonnes Parrain
        nameParrainColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailParrainColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneParrainColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressParrainColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        codeParrainageColumn.setCellValueFactory(new PropertyValueFactory<>("codeParrainage"));
        pointsParrainageColumn.setCellValueFactory(new PropertyValueFactory<>("parrainagePoints"));
        parrainTable.setItems(displayedParrains);
    }

    private void setupColumnResizePolicies() {
        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        parrainTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        
        // AJOUT: Politique de redimensionnement pour la table des comptes client
        remainingTimeClientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void setupRemainingTimeClientsTable() {
        nameRemainingClientColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneRemainingClientColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        remainingTimeColumn.setCellValueFactory(cellData -> {
            Client client = cellData.getValue();
            Duration remainingTime = calculateRemainingTimeForClient(client);
            String formattedTime = formatDuration(remainingTime);
            return new SimpleStringProperty(formattedTime);
        });

        setupActionsRemainingTimeColumn();
    }

    private void setupActionsRemainingTimeColumn() {
        actionsRemainingTimeColumn.setCellFactory(param -> new TableCell<Client, Void>() {
            private final Button restartSessionButton = new Button("Reprendre session");
            private final Button newReservationButton = new Button("Nouvelle réservation");

            {
                restartSessionButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
                restartSessionButton.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    restartSessionWithRemainingTime(client);
                });

                newReservationButton.setStyle("-fx-background-color: #34DBDBFF; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
                newReservationButton.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    openAddReservationWindowForClient(client);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Client client = getTableView().getItems().get(getIndex());
                    Duration remainingTime = calculateRemainingTimeForClient(client);
                    if (remainingTime.isZero() || remainingTime.isNegative()) {
                        setGraphic(null);
                    } else {
                        HBox buttons = new HBox(5, restartSessionButton, newReservationButton);
                        setGraphic(buttons);
                    }
                }
            }
        });
    }

    private void openAddReservationWindowForClient(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddReservationWindow.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            AddReservationController addReservationController = loader.getController();
            addReservationController.setParentController(ReservationController.getInstance());
            addReservationController.setConnectedUser(Fabrique.getService().getCurrentUser());

            // Pré-remplir les informations du client
            if (client != null) {
                addReservationController.setClientInfo(client);
            }

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la fenêtre de réservation.");
        }
    }

    /**
     * Configure la pagination pour chaque TableView.
     */
    private void setupPagination() {
        paginationClient.setPageFactory(this::createPageClient);
        paginationAdmin.setPageFactory(this::createPageUser);
        paginationParrain.setPageFactory(this::createPageParrain);
        paginationRemainingTime.setPageFactory(this::createPageRemainingTime);
    }

    /**
     * Met à jour le nombre total de pages pour une pagination donnée.
     */
    private void updatePaginationPageCount(Pagination pagination, int totalItems) {
        int pageCount = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        pagination.setPageCount(Math.max(1, pageCount));
    }

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

    // AJOUT: Méthode de pagination pour les comptes client
    private Node createPageRemainingTime(int pageIndex) {
        remainingTimeClientsTable.setItems(getRemainingTimeClientsPage(pageIndex));
        return remainingTimeClientsTable;
    }

    private ObservableList<User> getUsersPage(int pageIndex) {
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, displayedUsers.size());
        return FXCollections.observableArrayList(displayedUsers.subList(fromIndex, toIndex));
    }

    private ObservableList<Client> getClientsPage(int pageIndex) {
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, displayedClients.size());
        return FXCollections.observableArrayList(displayedClients.subList(fromIndex, toIndex));
    }

    private ObservableList<Parrain> getParrainsPage(int pageIndex) {
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, displayedParrains.size());
        return FXCollections.observableArrayList(displayedParrains.subList(fromIndex, toIndex));
    }

    // AJOUT: Méthode de pagination pour les comptes client
    private ObservableList<Client> getRemainingTimeClientsPage(int pageIndex) {
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, displayedRemainingTimeClients.size());
        return FXCollections.observableArrayList(displayedRemainingTimeClients.subList(fromIndex, toIndex));
    }

    /**
     * Méthode principale pour appliquer tous les filtres à toutes les sections.
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
                        matchesReservationDate = client.getReservations().stream()
                                .anyMatch(reservation -> {
                                    if (reservation.getReservationDate() != null) {
                                        LocalDate dateRes = reservation.getReservationDate().toLocalDate();
                                        return dateRes.equals(reservationDateFilter);
                                    }
                                    return false;
                                });
                    }
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
                .filter(user -> user.getRole().equals(Role.Admin))
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
        displayedParrains.sort((p1, p2) -> Integer.compare(p2.getParrainagePoints(), p1.getParrainagePoints()));
        updatePaginationPageCount(paginationParrain, displayedParrains.size());
        paginationParrain.setCurrentPageIndex(0);
        parrainTable.setItems(getParrainsPage(0));

        // AJOUT: Filtrer les comptes client avec temps restant
        String remainingTimeQuery = searchRemainingTimeField.getText() == null ? "" : searchRemainingTimeField.getText().toLowerCase();
        List<Client> filteredRemainingTimeClientsList = allRemainingTimeClients.stream()
                .filter(client -> {
                    boolean matchesName = client.getName().toLowerCase().contains(remainingTimeQuery);
                    boolean matchesPhone = client.getPhone() != null && client.getPhone().toLowerCase().contains(remainingTimeQuery);
                    return matchesName || matchesPhone;
                })
                .collect(Collectors.toList());
        displayedRemainingTimeClients.setAll(filteredRemainingTimeClientsList);
        updatePaginationPageCount(paginationRemainingTime, displayedRemainingTimeClients.size());
        paginationRemainingTime.setCurrentPageIndex(0);
        remainingTimeClientsTable.setItems(getRemainingTimeClientsPage(0));
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
     */
    public void refreshAllData() {
        allUsers.setAll(Fabrique.getService().findAllUsers());
        allClients.setAll(Fabrique.getService().getAllClients());
        allParrains.setAll(Fabrique.getService().getAllParrains());
        
        // AJOUT: Charger les clients avec temps restant
        List<Client> allClientsData = Fabrique.getService().getAllClients();
        List<Client> remainingTimeClients = allClientsData.stream()
            .filter(client ->
                client.getPhone() != null &&
                !client.getPhone().trim().isEmpty() &&
                client.getReservations() != null &&
                !client.getReservations().isEmpty()
            )
            .collect(Collectors.toList());
        allRemainingTimeClients.setAll(remainingTimeClients);
        
        applyFilters();
    }

    /**
     * Gère l'affichage/masquage des boutons en fonction du rôle de l'utilisateur connecté.
     */
    private void managePermissions() {
        User currentUser = ConnexionController.user;
        boolean isSuperAdmin = currentUser != null && currentUser.getRole().equals(Role.SuperAdmin);
        boolean isAdmin = currentUser != null && currentUser.getRole().equals(Role.Admin);

        if (addAdminButton != null) addAdminButton.setVisible(isSuperAdmin);
        if (editAdminButton != null) editAdminButton.setVisible(isSuperAdmin);
        if (deleteAdminButton != null) deleteAdminButton.setVisible(isSuperAdmin);

        if (editClientButton != null) editClientButton.setVisible(isSuperAdmin);
        if (deleteClientButton != null) deleteClientButton.setVisible(isSuperAdmin);

        if (addParrainButton != null) addParrainButton.setVisible(isSuperAdmin || isAdmin);
        if (editParrainButton != null) editParrainButton.setVisible(isSuperAdmin);
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
            stage.setOnHidden(event -> refreshAllData());
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
        openModalWindow("/com/views/AddClientWindow.fxml", "Ajouter un client", null);
    }

    @FXML
    private void openEditClientWindow() {
        if (ConnexionController.user == null || !ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires pour modifier un client.");
            return;
        }
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
        if (ConnexionController.user == null || !ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
            showAlert(AlertType.WARNING, "Accès refusé : Vous n'avez pas les permissions nécessaires pour modifier un parrain.");
            return;
        }
        Parrain selectedParrain = parrainTable.getSelectionModel().getSelectedItem();
        if (selectedParrain == null) {
            showAlert(AlertType.WARNING, "Veuillez sélectionner un parrain à modifier.");
            return;
        }
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
        if (ConnexionController.user != null && ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
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
        if (ConnexionController.user != null && ConnexionController.user.getRole().equals(Role.SuperAdmin)) {
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

    private Duration calculateRemainingTimeForClient(Client client) {
        if (client == null) {
            return Duration.ZERO;
        }

        List<GameSession> sessions = Fabrique.getService().findGameSessionsByClientId(client.getId());

        // Filtrer uniquement les sessions en pause avec un temps restant valide
        List<GameSession> validPausedSessions = sessions.stream()
            .filter(session -> "En pause".equals(session.getStatus()))
            .filter(session -> session.getPausedRemainingTime() != null)
            .filter(session -> !session.getPausedRemainingTime().isNegative())
            .filter(session -> !session.getPausedRemainingTime().isZero())
            .collect(Collectors.toList());

        // Trier les sessions par date de mise en pause (la plus récente en premier)
        validPausedSessions.sort((s1, s2) -> s2.getPausedRemainingTime().compareTo(s1.getPausedRemainingTime()));

        // Retourner uniquement le temps restant de la session en pause la plus récente
        if (!validPausedSessions.isEmpty()) {
            return validPausedSessions.get(0).getPausedRemainingTime();
        }

        return Duration.ZERO;
    }



    // Méthode pour formater la durée en "Xh Ymin"
    private String formatDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return "0h 0min";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%dh %dmin", hours, minutes);
    }

    public void loadClientsWithRemainingTime() {
        // Cette méthode est maintenant gérée par refreshAllData() et applyFilters()
        // Le tableau affiche automatiquement les données filtrées et paginées
    }

    // Redémarrer une session avec le temps restant
   private void restartSessionWithRemainingTime(Client client) {
    try {
        // 1. Vérifier temps restant valide
        Duration remainingTime = calculateRemainingTimeForClient(client);
        if (remainingTime.isZero() || remainingTime.isNegative()) {
            ControllerUtils.showInfoAlert("Aucun temps restant", 
                "Ce client n'a pas de temps restant disponible.");
            return;
        }

        // 2. Vérification CRITIQUE CORRIGÉE : seulement sessions ACTIVES
        List<GameSession> activeSessions = Fabrique.getService().getAllGameSessions().stream()
            .filter(s -> s.getClient() != null && s.getClient().getId() == client.getId())
            .filter(s -> "Active".equalsIgnoreCase(s.getStatus()))
            .collect(Collectors.toList());

        if (!activeSessions.isEmpty()) {
            GameSession activeSession = activeSessions.get(0);
            ControllerUtils.showErrorAlert("Session active existante",
                "Le client a déjà une session ACTIVE sur le poste " + 
                activeSession.getPoste().getName() + 
                ". Veuillez terminer cette session avant de reprendre le temps restant.");
            return;
        }

        // 3. OUVRIR fenêtre choix poste/jeu
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/ChoosePosteAndGame.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        
        ChoosePosteAndGameController controller = loader.getController();
        controller.setClient(client);
        controller.setRemainingTime(remainingTime);
        controller.setParentController(this);
        
        stage.setScene(scene);
        stage.setTitle("Reprendre session - " + formatDuration(remainingTime) + " restantes");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

    } catch (Exception e) {
        ControllerUtils.showErrorAlert("Erreur", "Impossible d'ouvrir la fenêtre de reprise: " + e.getMessage());
    }
}
}