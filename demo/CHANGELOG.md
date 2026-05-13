# Journal des modifications – GestionSalles Kay Play

## Version 1.3.5

### Général
- Version de l’application passée à **1.3.5** (`config.properties`, `AppConfig`).
- Bouton **réduire** la fenêtre déjà actif (minimiser).

### Utilisateurs / Compte client
- **Liste sans fin** : pagination remplacée par l’affichage de toute la liste des clients.
- **Super admin** : boutons Ajouter, Modifier, Supprimer client (sans passer par une réservation).
- **Admin** : uniquement le bouton « Ajouter un client ».
- **Modifier client** : seuls le **nom** et le **téléphone** sont modifiables.
- **Suppression** : impossible de supprimer un client qui a encore des **minutes restantes**.
- **Unicité du numéro** : impossible d’ajouter ou d’enregistrer un client avec un numéro de téléphone déjà existant.
- **Onglet Historique** (Compte client) : boutons **Voir** et **Générer PDF** ; PDF en **paysage** avec en-tête client et tableau des sessions (date résa, n° ticket, poste, jeu, durée, montant, points fidélité, statut).

### Postes et Jeux
- **Postes qui disparaissent** : après suppression d’un jeu par le super admin, la vue « Postes et Jeux » est rafraîchie pour que tous les postes restent visibles.

### Réservation
- Recherche client : **validation avec la touche Entrée**.
- **Recherche par filtre** : menu déroulant « Téléphone ou nom », « Téléphone uniquement », « Nom uniquement » ; si plusieurs clients correspondent au nom, une boîte de dialogue permet de choisir le client.
- **Ticket de réservation** : format **réduit** pour utiliser **moins de papier** (hauteur, polices et blocs allégés).

### Produits
- **Liste sans fin** : toute la liste des produits affichée.
- **Barre de recherche** : filtre en temps réel sur le nom.
- **Ticket produit** : format **réduit** pour utiliser **moins de papier**.
- **Catégories (super admin)** : entité `Categorie`, table `categories`, CRUD via la fenêtre « Catégories » (bouton sur l’écran Produits) ; assignation d’une catégorie à chaque produit à l’ajout et à la modification ; **filtrage** de la liste des produits par catégorie (menu déroulant « Toutes les catégories » ou choix d’une catégorie).

### Finance
- **Liste sans fin** : pagination remplacée par l’affichage complet.
- **Rapport journalier** (admin) : correction des erreurs (gestion `currentUser` null, try/catch, champs null-safe).

### Promotion
- **Chargement de la vue** : correction de l’erreur « Impossible de charger promo.fxml » (ajout de l’import `VBox` dans le FXML).

### Tickets (réservation et produit)
- Les formats des tickets **réservation** et **produit** ont été **réduits** pour **économiser le papier** (polices plus petites, interlignes et blocs allégés, suppression d’éléments superflus). Ce format compact est le format actuel retenu.

---

*Dernière mise à jour du journal : version 1.3.5.*
