# Guide d'Exécution / Execution Guide

## 🇫🇷 Français

### Installation rapide

1. **Vérifier Java JDK 21+** est installé :
   ```bash
   java --version
   ```
   Si vous n'avez pas Java 21+, téléchargez-le depuis [Adoptium](https://adoptium.net/) ou [Oracle](https://www.oracle.com/java/technologies/downloads/).

2. **Cloner le projet** (si ce n'est pas déjà fait) :
   ```bash
   git clone https://github.com/BoujuMaxime/PresidentGame.git
   cd PresidentGame
   ```

### Lancer l'application

#### Option 1 : Script d'exécution (Recommandé)

**Linux / macOS :**
```bash
./run.sh
```

**Windows :**
```cmd
run.bat
```

#### Option 2 : Gradle directement

**Lancer l'application :**
```bash
./gradlew run        # Linux/macOS
.\gradlew.bat run    # Windows
```

**Compiler le projet :**
```bash
./gradlew build
```

**Exécuter les tests :**
```bash
./gradlew test
```

### Notes

- L'application lance une interface graphique JavaFX
- Assurez-vous d'avoir une interface graphique disponible (pas de mode headless)
- Les fichiers de build sont générés dans le dossier `build/`
- Si `run.sh` n'est pas exécutable : `chmod +x run.sh`

---

## 🇬🇧 English

### Quick Installation

1. **Check Java JDK 21+** is installed:
   ```bash
   java --version
   ```
   If you don't have Java 21+, download it from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/).

2. **Clone the project** (if not already done):
   ```bash
   git clone https://github.com/BoujuMaxime/PresidentGame.git
   cd PresidentGame
   ```

### Run the Application

#### Option 1: Execution Script (Recommended)

**Linux / macOS:**
```bash
./run.sh
```

**Windows:**
```cmd
run.bat
```

#### Option 2: Direct Gradle

**Run the application:**
```bash
./gradlew run        # Linux/macOS
.\gradlew.bat run    # Windows
```

**Build the project:**
```bash
./gradlew build
```

**Run tests:**
```bash
./gradlew test
```

### Notes

- The application launches a JavaFX graphical interface
- Make sure you have a graphical interface available (not headless mode)
- Build files are generated in the `build/` folder
- If `run.sh` is not executable: `chmod +x run.sh`

---

## 🔧 Troubleshooting / Dépannage

### Java version incompatible / Incompatible Java version
If you get an error about Java version, make sure you have JDK 21 or higher installed.

### Permission denied (Linux/macOS)
If you get a permission error, make the script executable:
```bash
chmod +x run.sh
chmod +x gradlew
```

### Display error / Erreur d'affichage
JavaFX requires a graphical display. If you're on a headless server, you'll need to configure X11 forwarding or use a virtual display.

### Port already in use / Port déjà utilisé
If you get a port error, make sure no other instance of the application is running.
