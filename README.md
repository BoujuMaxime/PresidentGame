# PresidentGame 🃏

Un jeu de cartes "Président" (également connu sous le nom de "Trou du Cul") implémenté en Kotlin avec support pour les joueurs humains et l'intelligence artificielle.

## 📋 Description

PresidentGame est une implémentation du célèbre jeu de cartes "Président", un jeu de défausse stratégique où les joueurs tentent de se débarrasser de toutes leurs cartes le plus rapidement possible. Le premier joueur à vider sa main devient le "Président", tandis que le dernier devient le "Trou du Cul".

### À propos du jeu Président

Le Président est un jeu de cartes populaire qui se joue généralement avec 3 à 7 joueurs. Le jeu utilise un jeu standard de 52 cartes, avec des règles de hiérarchie spéciales où le 2 est la carte la plus forte et le 3 la plus faible. Ce projet permet de jouer contre des intelligences artificielles de différents niveaux ou en mode multijoueur.

## 🎮 Règles du jeu

### Objectif
Être le premier à se débarrasser de toutes ses cartes pour devenir le Président.

### Hiérarchie des cartes
Les cartes sont classées dans l'ordre suivant (de la plus faible à la plus forte) :
- 3 < 4 < 5 < 6 < 7 < 8 < 9 < 10 < Valet < Dame < Roi < As < 2

### Déroulement
1. **Distribution** : Les cartes sont distribuées équitablement entre tous les joueurs
2. **Premier tour** : Le joueur avec le 3 de trèfle commence (ou le joueur à gauche du donneur)
3. **Jouer des cartes** : Chaque joueur doit jouer une ou plusieurs cartes de même valeur supérieures ou égales à celles jouées précédemment
4. **Types de coups** : Le premier joueur d'un pli peut décider de jouer :
   - Une carte simple (SINGLE)
   - Une paire (PAIR)
   - Un brelan (3 cartes de même valeur)
   - Un carré (FOUR_OF_A_KIND - 4 cartes de même valeur)
   - Une suite (STRAIGHT - cartes consécutives)
5. **Suivre ou passer** : Les joueurs suivants doivent jouer le même nombre de cartes de rang supérieur ou égal, ou passer leur tour
6. **Fin du pli** : Quand tous les joueurs passent, le dernier joueur à avoir posé des cartes remporte le pli et commence un nouveau tour
7. **Victoire** : Le jeu continue jusqu'à ce qu'il ne reste qu'un seul joueur avec des cartes

### Règles spéciales

#### Carré Magique 🎴
Lorsqu'un joueur pose la quatrième carte d'une même valeur (complétant ainsi un carré), il remporte immédiatement le pli, quelle que soit la valeur des cartes. Cette règle peut être activée ou désactivée dans les paramètres du jeu (`withCarreMagique`).

#### Ta Gueule 🤫
Règle spéciale permettant de couper la parole à d'autres joueurs dans certaines situations. Cette règle peut être activée ou désactivée dans les paramètres du jeu (`withTaGueule`).

#### Règle du 2
Le 2 est la carte la plus forte. Jouer un 2 remporte immédiatement le pli.

#### Règle du Président
Si le premier joueur vide sa main pendant un pli, le joueur suivant remporte le pli (on ne joue pas sur le Président qui vient de terminer).

### Rôles sociaux et échanges de cartes
À la fin de chaque manche, les joueurs reçoivent des rôles selon leur classement, qui déterminent les échanges de cartes pour la manche suivante :

- **Président** 👑 : Le premier joueur à vider sa main
  - Reçoit les deux meilleures cartes du **Trou du Cul**
  - Donne en retour deux cartes de son choix (généralement les plus faibles)
  
- **Vice-Président** 🎖️ : Le deuxième joueur
  - Reçoit la meilleure carte du **Vice-Trou du Cul**
  - Donne en retour une carte de son choix
  
- **Neutre** 😐 : Les joueurs au milieu
  - Aucun échange de cartes
  
- **Vice-Trou du Cul** 😕 : L'avant-dernier joueur
  - Doit donner sa meilleure carte au **Vice-Président**
  - Reçoit en retour une carte
  
- **Trou du Cul** 💩 : Le dernier joueur avec des cartes
  - Doit donner ses deux meilleures cartes au **Président**
  - Reçoit en retour deux cartes

Ces échanges créent une dynamique où les bons joueurs conservent un avantage, tout en laissant la possibilité de renverser la hiérarchie.

## 🏗️ Architecture du projet

### Structure des dossiers

```
PresidentGame/
├── src/
│   ├── main/kotlin/
│   │   ├── Main.kt                           # Point d'entrée de l'application
│   │   └── model/
│   │       ├── Card.kt                       # Data class représentant une carte
│   │       ├── Game.kt                       # Classe principale gérant la logique du jeu
│   │       ├── Play.kt                       # Classe représentant un coup joué
│   │       ├── Utils.kt                      # Fonctions utilitaires (deck, shuffle, etc.)
│   │       └── player/
│   │           ├── Player.kt                 # Classe abstraite de base pour joueurs
│   │           ├── PlayerInterface.kt        # Interface définissant le contrat des joueurs
│   │           ├── PlayerUtils.kt            # Utilitaires pour la gestion des joueurs
│   │           ├── HumanPlayer.kt            # Implémentation pour joueur humain local
│   │           ├── RemoteHumanPlayer.kt      # Implémentation pour joueur distant (réseau)
│   │           └── ai/
│   │               ├── Ai.kt                 # Classe abstraite de base pour les IA
│   │               ├── AiInterface.kt        # Interface spécifique aux IA
│   │               ├── AiUtils.kt            # Utilitaires pour les algorithmes d'IA
│   │               ├── RandomAi.kt           # IA jouant des coups aléatoires valides
│   │               ├── EvaluateAi.kt         # IA évaluant chaque position avant de jouer
│   │               └── MiniMaxAi.kt          # IA utilisant l'algorithme MiniMax
│   └── test/kotlin/model/
│       ├── CardTest.kt                       # Tests unitaires pour Card
│       └── GameTest.kt                       # Tests unitaires pour Game (vide pour l'instant)
├── gradle/                                   # Fichiers wrapper Gradle
├── build.gradle.kts                          # Configuration Gradle du projet
├── settings.gradle.kts                       # Paramètres Gradle
├── gradlew                                   # Script Gradle pour Unix/Linux/macOS
├── gradlew.bat                               # Script Gradle pour Windows
└── README.md                                 # Ce fichier
```

### Composants principaux

#### 1. **Card** (`model/Card.kt`)
Data class représentant une carte à jouer avec :
- **Rank** : Énumération des valeurs (THREE, FOUR, FIVE, ..., ACE, TWO)
  - L'ordre correspond à la hiérarchie du jeu (THREE = plus faible, TWO = plus fort)
- **Suit** : Énumération des couleurs (CLUBS, DIAMONDS, HEARTS, SPADES)
- **Comparable** : Implémente la comparaison par rang pour faciliter le tri
- **Méthodes** : `equals()`, `hashCode()`, `compareTo()`, `toString()`

#### 2. **Play** (`model/Play.kt`)
Classe représentant un coup joué par un joueur :
- **cards** : Liste des cartes jouées dans ce coup
- **playType** : Type de combinaison (SINGLE, PAIR, STRAIGHT, FOUR_OF_A_KIND)
- Implémente `List<Card>` pour un accès facile aux cartes
- Utilisée pour valider et comparer les coups entre joueurs

#### 3. **Game** (`model/Game.kt`)
Classe centrale orchestrant le déroulement d'une partie :
- **GameParameters** : Configuration de la partie
  - `nbPlayers` : Nombre de joueurs (défaut : 4)
  - `gameMode` : LOCAL ou REMOTE
  - `aiDifficulty` : EASY, MEDIUM, HARD
  - `gameModeParameters` : Règles spéciales (Carré Magique, Ta Gueule)
- **Fonctions principales** :
  - `startGame()` : Démarre une partie complète
  - `resetDeck()` : Réinitialise et mélange le paquet
  - `distributeCards()` : Distribue les cartes équitablement
  - `exchangeCards()` : Gère les échanges selon les rôles
  - `playRound()` : Gère une manche complète
  - `assignRoles()` : Assigne les rôles selon le classement

#### 4. **Player System** (`model/player/`)
Architecture orientée objet pour gérer différents types de joueurs :

**Player** (classe abstraite) :
- Propriétés : `id`, `hand` (main), `role` (rôle social)
- Énumération Role : PRESIDENT, VICE_PRESIDENT, NEUTRAL, VICE_ASSHOLE, ASSHOLE
- Implémente `PlayerInterface`

**HumanPlayer** :
- Joueur humain en local (console ou interface graphique)
- Permet la saisie manuelle des coups

**RemoteHumanPlayer** :
- Joueur humain distant via réseau
- Support pour le mode multijoueur en ligne

**PlayerInterface** :
- Contrat définissant `playTurn()` que tous les joueurs doivent implémenter
- Paramètres : pile actuelle, défausse, dernier coup joué

#### 5. **AI System** (`model/player/ai/`)
Système d'intelligence artificielle avec plusieurs niveaux de complexité :

**Ai** (classe abstraite) :
- Hérite de `Player` et implémente `AiInterface`
- Base commune pour toutes les IA

**RandomAi** (Difficulté : EASY) :
- Choisit un coup valide aléatoirement parmi les coups possibles
- Rapide mais peu stratégique

**EvaluateAi** (Difficulté : MEDIUM) :
- Évalue chaque coup possible selon des heuristiques
- Prend en compte : nombre de cartes restantes, force des cartes, probabilité de victoire

**MiniMaxAi** (Difficulté : HARD) :
- Utilise l'algorithme MiniMax avec élagage alpha-bêta
- Simule plusieurs coups à l'avance
- Optimise la stratégie pour maximiser les chances de victoire

#### 6. **Utils** (`model/Utils.kt`)
Objet singleton fournissant des fonctions utilitaires :
- `createDeck()` : Crée un paquet de 52 cartes complet
- `clearDeck()` : Vide un paquet
- `verifyDeck()` : Vérifie l'intégrité d'un paquet (52 cartes uniques)
- `shuffleDeck()` : Mélange aléatoirement un paquet
- `printDeck()` / `printCard()` : Affichage pour le débogage

#### 7. **Main** (`Main.kt`)
Point d'entrée de l'application (en cours de développement) :
- Initialisation de la partie
- Gestion de l'interface utilisateur
- Boucle de jeu principale

## 🚀 Installation et configuration

### Prérequis

- **Java JDK 21** ou supérieur
- **Gradle** (inclus via Gradle Wrapper)
- **Kotlin 2.2.20**

### Installation

1. Clonez le dépôt :
```bash
git clone https://github.com/BoujuMaxime/PresidentGame.git
cd PresidentGame
```

2. Compilez le projet :
```bash
./gradlew build
```

3. Exécutez les tests :
```bash
./gradlew test
```

### Configuration de l'environnement de développement

Le projet utilise :
- **Gradle** comme système de build
- **Kotlin 2.2.20** avec JVM target 21
- **JUnit 5** pour les tests unitaires

## 💻 Utilisation

### Exécuter l'application

```bash
./gradlew run
```

### Lancer les tests

```bash
./gradlew test
```

### Compiler le projet

```bash
./gradlew build
```

## 🔧 Stack technique

### Langage et plateforme
- **Langage** : Kotlin 2.2.20
- **JVM Target** : Java 21 (via jvmToolchain)
- **Paradigme** : Orienté objet avec support fonctionnel

### Outils de build
- **Build Tool** : Gradle 8.14 avec Kotlin DSL
- **Wrapper** : Gradle Wrapper (gradlew) inclus pour reproductibilité
- **Configuration** : `build.gradle.kts` et `settings.gradle.kts`

### Tests
- **Framework** : JUnit 5 (JUnit Jupiter + JUnit Platform)
- **Assertions** : JUnit Assertions
- **Organisation** : Tests unitaires dans `src/test/kotlin/`

### Développement
- **IDE recommandé** : IntelliJ IDEA (support natif Kotlin)
- **Compatibilité** : Tout IDE supportant Gradle et Kotlin
- **Conventions** : KDoc pour la documentation, conventions Kotlin standard

### Architecture
- **Pattern** : MVC/Model-based avec séparation claire des responsabilités
- **Modularité** : Organisation en packages logiques (model, player, ai)
- **Extensibilité** : Interfaces et classes abstraites pour faciliter l'ajout de fonctionnalités

## 📊 État du développement

### Fonctionnalités implémentées ✅

#### Système de cartes
- ✅ Data class `Card` avec valeurs (Rank) et couleurs (Suit)
- ✅ Hiérarchie des cartes conforme aux règles du Président (3 = faible, 2 = forte)
- ✅ Comparaison de cartes via `Comparable`
- ✅ Tests unitaires complets pour `Card` (8 tests)

#### Système de jeu
- ✅ Classe `Play` représentant les coups (SINGLE, PAIR, STRAIGHT, FOUR_OF_A_KIND)
- ✅ Classe `Game` avec paramètres configurables
- ✅ Distribution équitable des cartes entre joueurs
- ✅ Système d'échanges de cartes basé sur les rôles
- ✅ Gestion des rôles sociaux (PRESIDENT, VICE_PRESIDENT, NEUTRAL, VICE_ASSHOLE, ASSHOLE)
- ✅ Support pour règles spéciales (Carré Magique, Ta Gueule)

#### Utilitaires
- ✅ `Utils` : création, mélange, vérification de paquet
- ✅ Affichage de cartes pour le débogage

#### Architecture joueurs
- ✅ Classe abstraite `Player` avec propriétés id, hand, role
- ✅ Interface `PlayerInterface` définissant le contrat
- ✅ Classes `HumanPlayer` et `RemoteHumanPlayer` (structure prête)
- ✅ `PlayerUtils` pour fonctions utilitaires

#### Architecture IA
- ✅ Classe abstraite `Ai` héritant de `Player`
- ✅ Interface `AiInterface` pour les IA
- ✅ Classes `RandomAi`, `EvaluateAi`, `MiniMaxAi` (structure prête)
- ✅ `AiUtils` pour algorithmes d'IA

### Fonctionnalités en cours de développement 🚧

#### Logique de jeu
- 🚧 Implémentation de `playPile()` dans `Game.kt` (gestion d'un pli complet)
- 🚧 Validation des coups joués (vérifier qu'un coup est valide)
- 🚧 Détection des conditions de victoire d'un pli (2, carré magique, tous passent)
- 🚧 Gestion du tour "on ne joue pas sur le président"

#### Implémentation des joueurs
- 🚧 Méthode `playTurn()` pour `HumanPlayer` (saisie utilisateur)
- 🚧 Méthode `playTurn()` pour `RemoteHumanPlayer` (communication réseau)
- 🚧 Mécanisme de passage de tour

#### Implémentation des IA
- 🚧 Algorithme complet de `RandomAi.playTurn()`
- 🚧 Heuristiques d'évaluation pour `EvaluateAi.playTurn()`
- 🚧 Algorithme MiniMax avec élagage alpha-bêta pour `MiniMaxAi.playTurn()`
- 🚧 Fonction d'évaluation de position

#### Interface utilisateur
- 🚧 Point d'entrée `Main.kt` fonctionnel
- 🚧 Interface en ligne de commande (CLI)
- 🚧 Affichage du jeu, des mains, des coups joués
- 🚧 Menu de configuration

#### Tests
- 🚧 Tests unitaires pour `Game` (GameTest.kt actuellement vide)
- 🚧 Tests pour `Play` et validation des coups
- 🚧 Tests d'intégration pour une partie complète

### Fonctionnalités prévues 🔮

#### Multijoueur
- 🔮 Mode multijoueur en ligne complet via `RemoteHumanPlayer`
- 🔮 Serveur de jeu pour héberger des parties
- 🔮 Système de lobby pour rejoindre des parties
- 🔮 Chat entre joueurs

#### Interface graphique
- 🔮 Interface graphique (JavaFX ou Compose Desktop)
- 🔮 Animations de cartes
- 🔮 Thèmes visuels personnalisables
- 🔮 Mode plein écran et fenêtré

#### Fonctionnalités avancées
- 🔮 Statistiques détaillées (taux de victoire, temps de jeu, etc.)
- 🔮 Historique des parties jouées
- 🔮 Replay de parties
- 🔮 Sauvegarde et chargement de parties en cours
- 🔮 Configuration avancée des règles (nombre de cartes à échanger, variantes locales)

#### Gameplay
- 🔮 Mode tournoi avec classement
- 🔮 Mode entraînement contre IA
- 🔮 Niveaux de difficulté supplémentaires
- 🔮 Système d'achievements/succès

#### Intelligence artificielle
- 🔮 IA avancée avec apprentissage automatique (reinforcement learning)
- 🔮 Profils d'IA avec styles de jeu différents (agressif, défensif, etc.)
- 🔮 Adaptation de l'IA au style du joueur

## 🧪 Tests

Le projet utilise JUnit 5 (JUnit Jupiter + JUnit Platform) pour les tests unitaires.

### Tests existants

#### CardTest.kt (8 tests ✅)
Tests complets pour la classe `Card` :
- ✅ `compareCardsWithDifferentRanks()` : Vérifie que ACE > KING
- ✅ `compareCardsWithSameRank()` : Vérifie l'égalité de rang
- ✅ `toStringReturnsCorrectFormatWithSuit()` : Format "RANK of SUIT"
- ✅ `toStringHandlesEmptySuitGracefully()` : Gestion des couleurs
- ✅ `equalsReturnsFalseWhenRankDiffers()` : Inégalité par rang
- ✅ `equalsAndHashCodeMatchForIdenticalCards()` : Égalité et hashCode
- ✅ `compareToReturnsNegativeForLowerRankCard()` : THREE < FIVE
- ✅ `compareToPlacesTwoAboveAce()` : TWO > ACE (règle spécifique)

#### GameTest.kt
Fichier de test créé mais vide (en attente d'implémentation).

### Commandes de test

Exécuter tous les tests :
```bash
./gradlew test
```

Exécuter les tests avec rapport détaillé :
```bash
./gradlew test --info
```

Exécuter les tests avec sortie console :
```bash
./gradlew test --console=verbose
```

Nettoyer et tester :
```bash
./gradlew clean test
```

### Couverture de code

La couverture actuelle se concentre sur les classes de base (Card). Les tests pour Game, Play, Player et AI seront ajoutés au fur et à mesure de l'implémentation de leurs fonctionnalités.

## 🤝 Contribution

Les contributions sont les bienvenues ! Que vous soyez un développeur Kotlin expérimenté ou un débutant, il y a plusieurs façons de contribuer à PresidentGame.

### Comment contribuer

1. **Forkez le projet** sur GitHub
2. **Clonez votre fork** localement :
   ```bash
   git clone https://github.com/votre-username/PresidentGame.git
   cd PresidentGame
   ```
3. **Créez une branche** pour votre fonctionnalité :
   ```bash
   git checkout -b feature/AmazingFeature
   ```
4. **Faites vos modifications** et testez-les :
   ```bash
   ./gradlew build
   ./gradlew test
   ```
5. **Committez vos changements** :
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
6. **Poussez vers votre fork** :
   ```bash
   git push origin feature/AmazingFeature
   ```
7. **Ouvrez une Pull Request** sur le dépôt principal

### Domaines de contribution

#### Pour les développeurs Kotlin 💻
- **Implémentation des IA** : RandomAi, EvaluateAi, MiniMaxAi
- **Logique de jeu** : Méthode `playPile()`, validation des coups
- **Tests** : Écrire des tests pour Game, Play, Player
- **Performance** : Optimisation des algorithmes d'IA
- **Refactoring** : Amélioration de la structure du code

#### Pour les développeurs UI/UX 🎨
- **Interface CLI** : Améliorer l'affichage en console
- **Interface graphique** : Créer une GUI avec JavaFX ou Compose
- **Design** : Thèmes visuels, animations de cartes

#### Pour les développeurs réseau 🌐
- **Multijoueur** : Implémentation de RemoteHumanPlayer
- **Serveur** : Créer un serveur de jeu
- **Protocol** : Définir un protocole de communication

#### Pour les testeurs 🧪
- **Tests manuels** : Jouer et rapporter des bugs
- **Tests automatisés** : Écrire des tests d'intégration
- **Documentation** : Améliorer ce README, ajouter des tutoriels

#### Pour les joueurs et enthousiastes 🎮
- **Règles** : Proposer des variantes de règles
- **Feedback** : Suggérer des améliorations de gameplay
- **Documentation** : Clarifier les règles, traduire en d'autres langues

### Standards de code

#### Conventions Kotlin
- Suivez les [conventions Kotlin officielles](https://kotlinlang.org/docs/coding-conventions.html)
- Utilisez `camelCase` pour les fonctions et variables
- Utilisez `PascalCase` pour les classes
- Indentation : 4 espaces

#### Documentation
- Documentez les fonctions publiques avec **KDoc** :
  ```kotlin
  /**
   * Description de la fonction.
   *
   * @param param Description du paramètre
   * @return Description du retour
   */
  fun maFonction(param: String): Int { ... }
  ```
- Commentez en **français** (langue du projet)
- Expliquez le "pourquoi", pas seulement le "quoi"

#### Tests
- Ajoutez des tests unitaires pour toute nouvelle fonctionnalité
- Nommage des tests : `nomDeLaFonction_contexteDuTest()` en français
- Assurez-vous que `./gradlew test` passe avant de soumettre
- Visez une couverture de code élevée

#### Pull Requests
- **Titre clair** : "Ajout de RandomAi" plutôt que "Update"
- **Description détaillée** : Expliquez ce qui a été fait et pourquoi
- **Commits atomiques** : Un commit = une fonctionnalité/correction
- **Messages de commit** : En français, impératif ("Ajoute" pas "Ajouté")
- **Tests passants** : Vérifiez que tous les tests passent
- **Pas de conflits** : Résolvez les conflits avant de soumettre

### Environnement de développement

#### IntelliJ IDEA (recommandé)
1. Ouvrez le projet avec "Open" (pas "Import")
2. IntelliJ détectera automatiquement Gradle
3. Attendez la synchronisation Gradle
4. Configuration run : Main.kt avec JVM 21

#### Autre IDE
- Assurez-vous que l'IDE supporte Kotlin et Gradle
- Configurez le SDK Java 21
- Importez le projet comme projet Gradle

### Besoin d'aide ?

- 💬 **Issues GitHub** : Pour questions, bugs, suggestions
- 📖 **Wiki** (à venir) : Documentation détaillée
- 📧 **Contact** : Voir section Contact ci-dessous

## 📝 License

Ce projet est actuellement sans licence spécifiée. Veuillez contacter l'auteur pour plus d'informations sur l'utilisation.

## 👤 Auteur

**Maxime Bouju** - [@BoujuMaxime](https://github.com/BoujuMaxime)

## 📞 Contact

### Auteur
- **GitHub** : [@BoujuMaxime](https://github.com/BoujuMaxime)

### Support et questions
- 🐛 **Bugs** : [Ouvrir une issue](https://github.com/BoujuMaxime/PresidentGame/issues/new?labels=bug)
- ✨ **Suggestions** : [Ouvrir une issue](https://github.com/BoujuMaxime/PresidentGame/issues/new?labels=enhancement)
- ❓ **Questions** : [Ouvrir une discussion](https://github.com/BoujuMaxime/PresidentGame/issues/new?labels=question)
- 💡 **Propositions** : N'hésitez pas à créer des issues ou des pull requests

---

## 🎓 Pour les développeurs débutants

### Comprendre le projet

Ce projet est une excellente opportunité d'apprendre :
- **Kotlin** : Langage moderne pour la JVM
- **POO** : Concepts d'héritage, interfaces, classes abstraites
- **Algorithmique** : IA avec MiniMax, heuristiques
- **Tests** : TDD avec JUnit 5
- **Gradle** : Gestion de build et dépendances

### Par où commencer ?

1. **Lire les règles** : Assurez-vous de bien comprendre le jeu Président
2. **Explorer Card.kt** : Classe simple et bien testée, bon point de départ
3. **Regarder CardTest.kt** : Exemples de tests unitaires
4. **Comprendre Game.kt** : Architecture du jeu (même avec TODO)
5. **Expérimenter** : Créez un petit main() pour manipuler les cartes

### Structure logique du code

```
Cartes (Card) → Coups (Play) → Joueurs (Player/AI) → Partie (Game)
```

1. Une **Card** a un rang et une couleur
2. Un **Play** est une ou plusieurs cartes jouées ensemble
3. Un **Player** joue des Plays à partir de sa main (hand)
4. Une **Game** orchestre les joueurs, les tours, et les règles

---

## 📄 License

Ce projet est actuellement sans licence spécifiée. Veuillez contacter l'auteur pour plus d'informations sur l'utilisation et la distribution.

---

## 🙏 Remerciements

Merci à tous les contributeurs actuels et futurs qui aident à faire de PresidentGame un projet de qualité !

---

**Note** : Ce projet est en cours de développement actif. Les fonctionnalités et l'architecture peuvent évoluer. Consultez régulièrement ce README pour les mises à jour.
