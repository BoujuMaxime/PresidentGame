# PresidentGame 🃏

Un jeu de cartes « Président » complet en Kotlin avec interface graphique JavaFX. Distribution, échanges, tours de jeu avec règles spéciales (Carré Magique, Ta Gueule) et attribution des rôles (Président, Vice-Président, Vice-Trou du Cul, Trou du Cul). Le projet propose une architecture MVC extensible avec système de thèmes, animations et IA configurables.

## 🎮 Captures d'écran

L'application propose 4 thèmes visuels différents :
- **Casino** : Style classique poker avec fond vert et accents dorés
- **Cartoon** : Couleurs vives et ludiques avec dégradés violets/roses
- **Sombre Élégant** : Thème dark avec accents néon rose/violet
- **Minimaliste** : Design épuré blanc/bleu Material Design

## Vue d'ensemble

- **Langage** : Kotlin 2.2.20
- **Interface** : JavaFX 21 avec CSS personnalisé
- **Cible JVM** : Java 21 (défini via `kotlin.jvmToolchain(21)`)
- **Build** : Gradle Kotlin DSL (wrapper `gradlew` / `gradlew.bat` inclus)
- **Tests** : JUnit 5 avec `useJUnitPlatform()`
- **Entrée** : `src/main/kotlin/Main.kt` lance l'application JavaFX

## ✨ Fonctionnalités

### Interface Graphique
- ✅ **Menu de configuration** : Sélection du nombre de joueurs (3-6), difficulté IA, règles spéciales
- ✅ **Sélecteur de thèmes** : 4 thèmes visuels interchangeables en temps réel
- ✅ **Plateau de jeu interactif** : Vue des adversaires, pile centrale, main du joueur
- ✅ **Sélection de cartes** : Clic pour sélectionner, validation automatique des combinaisons
- ✅ **Animations** : Hover, sélection, distribution, jeu de cartes
- ✅ **Journal d'événements** : Historique scrollable des actions de la partie
- ✅ **Dialogues modaux** : Fin de manche avec classement, échange de cartes

### Règles du Jeu
- ✅ Distribution cyclique des 52 cartes
- ✅ Combinaisons : Simple, Paire, Brelan, Carré
- ✅ Échange de cartes selon les rôles (Président ↔ Trou du Cul)
- ✅ Règle "Carré Magique" : Un carré termine le pli
- ✅ Règle "Ta Gueule" : Forcer la même valeur après 2 cartes identiques consécutives

### Intelligence Artificielle
- ✅ **RandomAi** : Joue aléatoirement parmi les coups valides
- ✅ **EvaluateAi** : Stratégie basique (à améliorer)
- 🚧 **MiniMaxAi** : En développement

## 🏗️ Architecture

### Pattern MVC

```
┌─────────────────────────────────────────────────────────────────┐
│                           VIEW (JavaFX)                         │
├─────────────────────────────────────────────────────────────────┤
│  PresidentGameApp    │  MenuView      │  GameBoardView          │
│  (Navigation)        │  (Config)      │  (Plateau de jeu)       │
├──────────────────────┼────────────────┼─────────────────────────┤
│  Components:         │  Dialogs:      │  Themes:                │
│  - CardView          │  - InGameMenu  │  - ThemeManager         │
│  - PlayerPanel       │  - RoundFinish │  - base.css             │
│  - PileView          │  - CardExchange│  - casino/cartoon/...   │
│  - PlayerHandView    │                │                         │
│  - ActionBar         │                │                         │
│  - EventLog          │                │                         │
└──────────────────────┴────────────────┴─────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        CONTROLLER                               │
├─────────────────────────────────────────────────────────────────┤
│  GameController      │  GuiHumanPlayer  │  ObservableAi         │
│  - Properties obs.   │  - Future-based  │  - Wrapper IA         │
│  - État de la partie │  - Sélection UI  │  - Notifications      │
└──────────────────────┴──────────────────┴───────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                          MODEL                                  │
├─────────────────────────────────────────────────────────────────┤
│  Game / GameTurns    │  Card / PlayerMove  │  Player / AI       │
│  - Cycle de partie   │  - Domaine cartes   │  - Abstraction     │
│  - Règles spéciales  │  - Combinaisons     │  - Stratégies      │
└──────────────────────┴─────────────────────┴────────────────────┘
```

### Organisation des sources

```
PresidentGame/
├── src/main/kotlin/
│   ├── Main.kt                          # Point d'entrée JavaFX
│   │
│   ├── controller/                      # Contrôleurs MVC
│   │   ├── GameController.kt            # Contrôleur principal avec propriétés observables
│   │   ├── GuiHumanPlayer.kt            # Joueur humain connecté à l'UI
│   │   └── ObservableAi.kt              # Wrapper IA avec notifications
│   │
│   ├── view/                            # Interface graphique JavaFX
│   │   ├── PresidentGameApp.kt          # Application principale, navigation
│   │   ├── MenuView.kt                  # Menu de configuration
│   │   ├── GameBoardView.kt             # Plateau de jeu principal
│   │   │
│   │   ├── components/                  # Composants réutilisables
│   │   │   ├── CardView.kt              # Vue d'une carte (face/dos)
│   │   │   ├── PlayerPanel.kt           # Panneau joueur adverse
│   │   │   ├── PileView.kt              # Pile centrale de cartes
│   │   │   ├── PlayerHandView.kt        # Main du joueur humain
│   │   │   ├── ActionBar.kt             # Boutons Jouer/Passer/Trier
│   │   │   └── EventLog.kt              # Journal des événements
│   │   │
│   │   ├── dialogs/                     # Dialogues modaux
│   │   │   ├── InGameMenuView.kt        # Menu pause
│   │   │   ├── RoundFinishedDialog.kt   # Fin de manche
│   │   │   └── CardExchangeDialog.kt    # Échange de cartes
│   │   │
│   │   └── themes/                      # Système de thèmes
│   │       └── ThemeManager.kt          # Gestion des thèmes CSS
│   │
│   └── model/                           # Modèle de données
│       ├── Card.kt                      # Carte (Rank + Suit)
│       ├── PlayerMove.kt                # Combinaison jouée
│       ├── Utils.kt                     # Utilitaires deck/affichage
│       ├── Game.kt                      # Orchestration partie
│       ├── GameTurns.kt                 # Gestion des plis
│       └── player/
│           ├── Player.kt                # Classe abstraite joueur
│           ├── PlayerInterface.kt       # Interface de jeu
│           ├── PlayerUtils.kt           # Calcul coups possibles
│           ├── HumanPlayer.kt           # Joueur console (TODO)
│           ├── RemoteHumanPlayer.kt     # Joueur réseau (TODO)
│           └── ai/
│               ├── Ai.kt                # Base IA
│               ├── AiInterface.kt       # Interface IA
│               ├── AiUtils.kt           # Helpers sélection
│               ├── RandomAi.kt          # IA aléatoire
│               ├── EvaluateAi.kt        # IA évaluative
│               └── MiniMaxAi.kt         # IA MiniMax (TODO)
│
├── src/main/resources/
│   └── themes/                          # Fichiers CSS des thèmes
│       ├── base.css                     # Styles communs (thème Casino)
│       ├── casino.css                   # Surcharges Casino
│       ├── cartoon.css                  # Thème Cartoon
│       ├── dark.css                     # Thème Sombre
│       └── minimal.css                  # Thème Minimaliste
│
└── src/test/kotlin/
    ├── controller/
    │   └── GuiHumanPlayerTest.kt
    └── model/
        ├── AiTest.kt
        ├── CardTest.kt
        ├── GameTest.kt
        ├── PlayerUtilsTest.kt
        └── PlayTest.kt
```

## 🚀 Installation et Exécution

### Prérequis

1. **Java JDK 21** ou supérieur
2. Wrapper Gradle fourni (`gradlew`, `gradlew.bat`)

### Commandes

```powershell
# Compilation
./gradlew clean build

# Tests
./gradlew test

# Exécution de l'application
./gradlew run
```

## 🎨 Système de Thèmes

L'application utilise un système de thèmes CSS modulaire :

### Structure
- `base.css` : Contient tous les styles de base (thème Casino par défaut)
- `{theme}.css` : Surcharge les styles pour personnaliser l'apparence

### Créer un nouveau thème

1. Créer un fichier `src/main/resources/themes/mon-theme.css`
2. Surcharger les classes CSS souhaitées
3. Ajouter le thème dans `ThemeManager.kt` :

```kotlin
val availableThemes: Map<String, String> = mapOf(
    // ...existing themes...
    "mon-theme" to "🎯 Mon Thème"
)
```

### Classes CSS principales

| Classe | Description |
|--------|-------------|
| `.game-board` | Fond du plateau de jeu |
| `.menu-panel` | Panneau du menu principal |
| `.card-container` | Conteneur d'une carte |
| `.card-container-selected` | Carte sélectionnée |
| `.player-box` | Boîte d'un joueur adverse |
| `.pile-stack` | Zone de la pile centrale |
| `.action-button` | Boutons d'action |

## 📖 Guide d'utilisation

### Menu Principal
1. **Nombre de joueurs** : Glissez le curseur (3-6 joueurs)
2. **Difficulté IA** : Facile / Moyen / Difficile
3. **Règles spéciales** : Activez/désactivez Carré Magique et Ta Gueule
4. **Thème** : Choisissez parmi les 4 thèmes disponibles
5. Cliquez sur **Démarrer la partie**

### Plateau de Jeu
1. **Sélection** : Cliquez sur vos cartes pour les sélectionner (même rang uniquement)
2. **Jouer** : Cliquez sur le bouton vert pour jouer votre combinaison
3. **Passer** : Cliquez sur le bouton rouge si vous ne pouvez/voulez pas jouer
4. **Trier** : Réorganise votre main par rang
5. **Menu** : Bouton ☰ en haut à gauche pour pause/nouvelle partie/quitter

### Fin de Manche
- Un dialogue affiche le classement avec les rôles attribués
- Choisissez de continuer (nouvelle manche) ou retourner au menu

## 🔧 Configuration du Projet

### build.gradle.kts

```kotlin
plugins {
    kotlin("jvm") version "2.2.20"
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

kotlin {
    jvmToolchain(21)
}
```

## 🗺️ Roadmap

### Terminé ✅
- [x] Interface graphique JavaFX complète
- [x] Système de thèmes CSS modulaire (4 thèmes)
- [x] Menu de configuration interactif
- [x] Plateau de jeu avec adversaires et pile
- [x] Sélection et validation des cartes
- [x] Journal d'événements
- [x] Dialogues fin de manche et échange
- [x] Animations de base (hover, sélection)

### En cours 🚧
- [ ] Animations avancées (distribution, jeu vers pile)
- [ ] Bulles de dialogue au-dessus des adversaires
- [ ] Sons et effets audio
- [ ] IA EvaluateAi améliorée
- [ ] IA MiniMax fonctionnelle

### Planifié 📋
- [ ] Mode multijoueur réseau
- [ ] Statistiques et historique des parties
- [ ] Personnalisation des règles avancée
- [ ] Mode tutoriel
- [ ] Sauvegarde/chargement de parties

## 🤝 Contribution

1. Forkez le dépôt et créez une branche dédiée (`feature/ma-feature`)
2. Travaillez avec `./gradlew build`, `./gradlew test` pour valider
3. Respectez le style Kotlin et la documentation KDoc
4. Ouvrez une PR décrivant les changements

## 📄 Licence

Projet sans licence définie — contactez l'auteur pour plus de détails.

---

**Auteur** : BOUJU Maxime  
**Version** : 1.0-SNAPSHOT
