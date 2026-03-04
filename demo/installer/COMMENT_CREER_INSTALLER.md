# Comment créer l’installateur GestionSalle_Setup_1.3.5.exe

L’installateur est généré en **2 étapes** : préparer un dossier avec l’exe + JAR + JRE, puis compiler le script **Inno Setup**.

---

## Étape 1 : Préparer le dossier « GestionSalleExe »

Crée un dossier (par ex. `C:\Users\HP\Desktop\GestionSalleExe`) et mets-y tout ce que l’application doit contenir.

### 1.1 Construire le JAR avec Maven

Dans le projet (dossier `demo`) :

```powershell
cd "D:\projts\projet fini\code\GestionSalle\demo"
.\mvnw.cmd clean package -DskipTests
```

Le JAR généré se trouve ici :  
`demo\target\demo-1.0-SNAPSHOT-jar-with-dependencies.jar`  
→ **Copie ce fichier** dans `GestionSalleExe`.

### 1.2 Créer GestionSalle.exe avec Launch4j

L’installateur attend un **fichier .exe** qui lance le JAR. On utilise **Launch4j** pour ça.

1. **Télécharger Launch4j** : https://launch4j.sourceforge.net/ (ou via le site officiel).
2. **Lancer Launch4j** et configurer :
   - **Output file** : `C:\Users\HP\Desktop\GestionSalleExe\GestionSalle.exe`
   - **Jar** : `C:\Users\HP\Desktop\GestionSalleExe\demo-1.0-SNAPSHOT-jar-with-dependencies.jar`
   - **Don’t use JRE** : décoché
   - **Bundled JRE path** : `JRE` (dossier relatif à côté de l’exe)
   - **Main class** : laisser vide (le JAR a un `Main-Class` dans le manifest).
   - Si besoin, onglet **JRE** : **Min JRE version** = `21` (ou la version que tu utilises).
3. Cliquer sur l’icône **engrenage** pour générer `GestionSalle.exe` dans `GestionSalleExe`.

### 1.3 Ajouter un JRE dans le dossier

L’exe Lance4j lance `JRE\bin\java.exe -jar ...`. Il faut donc un JRE à côté.

1. Prendre un **JRE 21** (ou la même version que ton projet) :
   - Soit extraire le JRE du JDK installé : dans ton JDK, le dossier `jre` ou le répertoire racine (bin, lib, etc.) peut servir de JRE.
   - Soit télécharger un JRE seul (Adoptium / Eclipse Temurin, etc.).
2. Copier **tout le contenu** de ce JRE dans :  
   `GestionSalleExe\JRE\`  
   de sorte que `GestionSalleExe\JRE\bin\java.exe` existe.

### 1.4 (Optionnel) Base SQLite vierge

Si tu veux fournir une base vide avec l’installateur :

- Copie une base `gestionsalles.sqlite` (vide ou avec schéma minimal) dans `GestionSalleExe\`.  
Le script Inno Setup l’installe dans `%APPDATA%\GestionSalles\` seulement si le fichier n’existe pas déjà.

### 1.5 Vérifier le contenu de GestionSalleExe

À la fin, le dossier doit contenir au minimum :

- `GestionSalle.exe`
- `demo-1.0-SNAPSHOT-jar-with-dependencies.jar` (ou le nom exact de ton JAR)
- `JRE\` (avec `JRE\bin\java.exe`, etc.)
- Optionnel : `gestionsalles.sqlite`

---

## Étape 2 : Adapter et compiler le script Inno Setup

### 2.1 Chemins dans le script

Le fichier `GestionSalle_Setup.iss` utilise des chemins en dur. **Si ton dossier n’est pas** `C:\Users\HP\Desktop\GestionSalleExe` **ou** si le projet n’est pas sur le Bureau, ouvre `GestionSalle_Setup.iss` et modifie :

- **Source (dossier des exe/jar/jre)**  
  Remplace `C:\Users\HP\Desktop\GestionSalleExe` par le chemin de ton dossier (lignes 45, 47, 50, 51, 54).
- **OutputDir**  
  Dossier où Inno Setup va créer le `.exe` d’installation (ligne 26), ex. :  
  `OutputDir=C:\Users\HP\Desktop\Setup`
- **SetupIconFile**  
  Chemin vers `kayplay-logo.ico` (ligne 27).  
  Ex. : `SetupIconFile=D:\projts\projet fini\code\GestionSalle\demo\installer\kayplay-logo.ico`

### 2.2 Installer Inno Setup

- Téléchargement : https://jrsoftware.org/isdl.php  
- Installer puis ouvrir **Inno Setup Compiler**.

### 2.3 Compiler le script

1. **Fichier → Ouvrir** : choisis `GestionSalle_Setup.iss` (dans `demo\installer\`).
2. **Build → Compiler** (ou F9).
3. À la fin, l’installateur est créé dans le dossier **OutputDir** défini dans le script (souvent `C:\Users\HP\Desktop\Setup`), avec un nom du type :  
   **GestionSalle_Setup_1.3.5.exe**

---

## Résumé des commandes / étapes

| Étape | Action |
|-------|--------|
| 1 | `cd demo` puis `.\mvnw.cmd clean package -DskipTests` |
| 2 | Copier le JAR depuis `target\` vers `GestionSalleExe\` |
| 3 | Créer `GestionSalle.exe` avec Launch4j (JAR + JRE relatif `JRE`) |
| 4 | Copier un JRE dans `GestionSalleExe\JRE\` |
| 5 | Ajuster les chemins dans `GestionSalle_Setup.iss` si besoin |
| 6 | Ouvrir le .iss dans Inno Setup et compiler (F9) |

Le fichier final à publier sur GitHub Releases est : **GestionSalle_Setup_1.3.5.exe**.
