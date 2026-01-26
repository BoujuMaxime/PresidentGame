# PresidentGame 🃏

Une base en Kotlin pour expérimenter le jeu de cartes « Président » : distribution, échanges, tours de jeu avec règles spéciales (Carré Magique, Force Play) et attribution des rôles (Président, Vice-Président, Vice-Trou du Cul, Trou du Cul). Le projet expose l’architecture nécessaire pour piloter des IA (Random, Evaluate, MiniMax) et poser les fondations d’un vrai client humain ou distant.

> **🚀 Pour lancer l'application rapidement, consultez [EXECUTION_GUIDE.md](EXECUTION_GUIDE.md)**

## Vue d’ensemble

- **Langage** : Kotlin 2.2.20
- **Cible JVM** : Java 21 (défini via `kotlin.jvmToolchain(21)` dans `build.gradle.kts`)
- **Build** : Gradle Kotlin DSL (wrapper `gradlew` / `gradlew.bat` inclus)
- **Tests** : JUnit 5 avec `useJUnitPlatform()`
- **Entrée** : `src/main/kotlin/Main.kt` lance deux parties consécutives (deux IA `EvaluateAi`/`RandomAi`) et affiche un résumé des mains et rôles.

## Architecture principale

### Domaine des cartes
- `model/Card.kt` : représentation `Rank` (de 3 à 2) et `Suit` (Trèfle, Carreau, Cœur, Pique), comparables et affichables.
- `model/PlayerMove.kt` : encapsule une combinaison jouée (SINGLE, PAIR, THREE_OF_A_KIND, FOUR_OF_A_KIND) avec validation et logique `canBePlayedOn`.
- `model/Utils.kt` : création/mélange/vérification du deck, suivi des états de jeu via les helpers `printPlay`, `printAction`, `printRolesSummary`, etc.

### Mécanique de partie
- `model/Game.kt` orchestre le cycle complet (validation du nombre de joueurs, distribution, échanges de cartes selon les rôles précédents, appel à `RoundManager`, attribution finale des rôles).
- `model/GameTurns.kt` gère les piles, les tours des joueurs, la détection des passes, les règles spéciales (`Carré Magique`, `Force Play`), la mise à jour du classement et la terminaison d’un pli.

### Joueurs et IA
- `model/player/Player.kt` : base abstraite avec `id`, main mutable et énumération des rôles.
- `PlayerInterface` définit `playTurn()` et `giveCardsToPlayer()`.
- `PlayerUtils` trie les mains et génère les coups possibles en appliquant `lastPlayerMove` et la contrainte de suite (`straightRank`).
- `HumanPlayer` & `RemoteHumanPlayer` sont des `TODO` prêts à recevoir de l’input externe.
- `model/player/ai/` contient :
  - `Ai` + `AiInterface` héritant du système joueur.
  - `RandomAi` choisit un coup aléatoire parmi les coups valides.
  - `EvaluateAi` est prévu pour analyser les positions (implémentation à compléter).
  - `MiniMaxAi` délègue temporairement à `EvaluateAi` mais ouvre la voie à un vrai MiniMax adaptatif.
  - `AiUtils` fournit des helpers de sélection (`chooseRandomPlay`, `chooseLowestPlay`).

## Fonctionnalités et état actuel

- ✅ Paquet de 52 cartes bien défini et vérifié, avec affichage et mélange dans `Utils`.
- ✅ Distribution cyclique des cartes et échange automatique entre rôles (Président ↔ Trou du Cul, Vice-Président ↔ Vice-Trou du Cul).
- ✅ `RoundManager` orchestre les tours, détecte les passes, applique les règles spéciales et maintient un classement dynamique.
- ✅ Architecture extensible pour intégrer des IA plus poussées et des clients humains.
- ✅ Tests unitaires sur `Card` (comparaison, égalité, `toString`).
- 🚧 Les interfaces humaines et AI d’évaluation restent à implémenter.

## Prérequis

1. Java JDK 21 ou supérieur.
2. Wrapper Gradle fourni (`gradlew`, `gradlew.bat`).
3. Console UTF-8 (le `main` force déjà UTF-8 pour `System.out` et `System.err`).

## 🚀 Démarrage rapide (Quick Start)

### Exécution en un clic

**Linux / macOS :**
```bash
./run.sh
```

**Windows :**
```cmd
run.bat
```

Ces scripts compilent automatiquement le projet et lancent l'application JavaFX.

### Exécution manuelle avec Gradle

**Pour lancer l'application directement :**
```bash
./gradlew run        # Linux/macOS
.\gradlew.bat run    # Windows
```

**Pour compiler le projet :**
```bash
./gradlew clean build
```

**Pour exécuter les tests :**
```bash
./gradlew test
```

## Compilation, tests et exécution

Lancez les commandes depuis la racine. Sous PowerShell, les deux variantes (Unix-like et Windows) sont valides :

```powershell
./gradlew clean build
./gradlew test
./gradlew run
```

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
.\gradlew.bat run
```

- `clean build` compile les sources et produit `PresidentGame-1.0-SNAPSHOT.jar` dans `build/libs`.
- `test` exécute la suite JUnit 5 (notamment `CardTest`).
- `run` exécute `Main.kt`, qui joue deux parties d’IA et affiche leur statut.

## Organisation des sources

```
PresidentGame/
├── src/main/kotlin/
│   ├── Main.kt                      # Lance l'application JavaFX
│   ├── controller/                  # Contrôleurs MVC
│   │   ├── GameController.kt        # Contrôleur principal
│   │   ├── GuiHumanPlayer.kt        # Joueur humain GUI
│   │   └── ObservableAi.kt          # Wrapper IA observable
│   ├── view/                        # Vues JavaFX
│   │   ├── PresidentGameApp.kt      # Application principale
│   │   ├── MenuView.kt              # Menu de configuration
│   │   └── GameBoardView.kt         # Plateau de jeu
│   └── model/                       # Modèle de données
│       ├── Card.kt
│       ├── PlayerMove.kt
│       ├── Utils.kt
│       ├── Game.kt
│       ├── GameTurns.kt
│       └── player/
│           ├── Player.kt
│           ├── PlayerInterface.kt
│           ├── PlayerUtils.kt
│           ├── HumanPlayer.kt
│           ├── RemoteHumanPlayer.kt
│           └── ai/
│               ├── Ai.kt
│               ├── AiInterface.kt
│               ├── AiUtils.kt
│               ├── RandomAi.kt
│               ├── EvaluateAi.kt
│               └── MiniMaxAi.kt
└── src/test/kotlin/model/
    ├── AiTest.kt
    ├── CardTest.kt
    ├── GameTest.kt
    ├── PlayerUtilsTest.kt
    └── PlayTest.kt
```

## Roadmap

1. ✅ Interface graphique JavaFX complète avec architecture MVC
2. ✅ Menu de configuration et plateau de jeu interactif
3. 🚧 Améliorer l'UI/UX avec animations et effets visuels
4. 🚧 Finaliser `EvaluateAi` et ajouter des tests de stratégie supplémentaires
5. 🚧 Implémenter l'IA `MiniMax` réelle et documenter les scénarios de parties
6. 🚧 Ajouter le mode multijoueur en réseau avec `RemoteHumanPlayer`
## Contribution

1. Forkez le dépôt et créez une branche dédiée (`feature/…`).
2. Travaillez avec `./gradlew build`, `./gradlew test` pour valider vos modifications.
3. Ouvrez une PR décrivant les changements et les tests effectués.

## Licence

Projet sans licence définie — contactez l’auteur pour plus de détails.
