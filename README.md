# PresidentGame 🃏

Un jeu de cartes "Président" (également connu sous le nom de "Trou du Cul") implémenté en Kotlin avec support pour les joueurs humains et l'intelligence artificielle.

## 📋 Description

PresidentGame est une implémentation du célèbre jeu de cartes "Président", un jeu de défausse stratégique où les joueurs tentent de se débarrasser de toutes leurs cartes le plus rapidement possible. Le premier joueur à vider sa main devient le "Président", tandis que le dernier devient le "Trou du Cul".

### À propos du jeu Président

Le Président est un jeu de cartes populaire qui se joue généralement avec 3 à 7 joueurs. Le jeu utilise un jeu standard de 52 cartes, avec des règles de hiérarchie spéciales où le 2 est la carte la plus forte et le 3 la plus faible.

## 🎮 Règles du jeu

### Objectif
Être le premier à se débarrasser de toutes ses cartes pour devenir le Président.

### Hiérarchie des cartes
Les cartes sont classées dans l'ordre suivant (de la plus faible à la plus forte) :
- 3 < 4 < 5 < 6 < 7 < 8 < 9 < 10 < Valet < Dame < Roi < As < 2

### Déroulement
1. Les cartes sont distribuées équitablement entre tous les joueurs
2. Le joueur avec le 3 de trèfle commence (ou le joueur à gauche du donneur)
3. Chaque joueur doit jouer une ou plusieurs cartes de même valeur supérieures à celles jouées précédemment
4. Si un joueur ne peut pas ou ne veut pas jouer, il passe son tour
5. Quand tous les joueurs passent, le dernier joueur à avoir posé des cartes remporte le pli et commence un nouveau tour
6. Le jeu continue jusqu'à ce qu'il ne reste qu'un seul joueur avec des cartes

### Rôles sociaux
- **Président** : Le premier joueur à vider sa main
- **Vice-Président** : Le deuxième joueur
- **Neutre** : Les joueurs au milieu
- **Vice-Trou du Cul** : L'avant-dernier joueur
- **Trou du Cul** : Le dernier joueur avec des cartes

## 🏗️ Architecture du projet

### Structure des dossiers

```
PresidentGame/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       ├── Main.kt                    # Point d'entrée de l'application
│   │       └── model/
│   │           ├── Card.kt                # Classe représentant une carte
│   │           ├── Game.kt                # Logique principale du jeu
│   │           ├── Utils.kt               # Utilitaires pour la gestion du jeu
│   │           └── player/
│   │               ├── Player.kt          # Classe abstraite de base pour tous les joueurs
│   │               ├── PlayerInterface.kt # Interface pour les actions des joueurs
│   │               ├── PlayerUtils.kt     # Utilitaires pour les joueurs
│   │               ├── HumanPlayer.kt     # Joueur humain local
│   │               ├── RemoteHumanPlayer.kt # Joueur humain distant
│   │               └── ai/
│   │                   ├── Ai.kt          # Classe abstraite de base pour les IA
│   │                   ├── AiInterface.kt # Interface pour les IA
│   │                   ├── AiUtils.kt     # Utilitaires pour les IA
│   │                   ├── RandomAi.kt    # IA jouant aléatoirement
│   │                   ├── EvaluateAi.kt  # IA avec évaluation de position
│   │                   └── MiniMaxAi.kt   # IA utilisant l'algorithme MiniMax
│   └── test/
│       └── kotlin/
│           └── model/
│               └── CardTest.kt            # Tests unitaires pour la classe Card
├── build.gradle.kts                       # Configuration Gradle
├── settings.gradle.kts                    # Paramètres du projet
└── README.md                              # Ce fichier
```

### Composants principaux

#### 1. **Card** (`model/Card.kt`)
Représente une carte à jouer avec :
- **Rank** : Valeur de la carte (THREE à TWO)
- **Suit** : Couleur de la carte (CLUBS, DIAMONDS, HEARTS, SPADES)
- Méthodes de comparaison et d'affichage

#### 2. **Player System** (`model/player/`)
Système de joueurs avec plusieurs types :
- **Player** : Classe abstraite de base
- **HumanPlayer** : Pour les joueurs humains locaux
- **RemoteHumanPlayer** : Pour les joueurs humains distants (multijoueur)
- **PlayerInterface** : Définit le contrat pour tous les joueurs

#### 3. **AI System** (`model/player/ai/`)
Système d'intelligence artificielle avec plusieurs stratégies :
- **RandomAi** : Joue des coups aléatoires
- **EvaluateAi** : Évalue les positions avant de jouer
- **MiniMaxAi** : Utilise l'algorithme MiniMax pour optimiser les décisions

#### 4. **Game** (`model/Game.kt`)
Gère la logique principale du jeu (en cours de développement)

#### 5. **Utils** (`model/Utils.kt`)
Fournit des utilitaires pour :
- Créer un jeu de cartes complet
- Mélanger le jeu
- Distribuer les cartes
- Afficher les cartes

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

## 🔧 Technologies utilisées

- **Langage** : Kotlin 2.2.20
- **JVM** : Java 21
- **Build Tool** : Gradle avec Kotlin DSL
- **Testing** : JUnit 5 (JUnit Platform)
- **IDE recommandé** : IntelliJ IDEA

## 📊 État du développement

### Fonctionnalités implémentées ✅

- ✅ Système de cartes avec valeurs et couleurs
- ✅ Hiérarchie des cartes conforme aux règles du Président
- ✅ Utilitaires de gestion du jeu (création, mélange, distribution)
- ✅ Architecture de base pour les joueurs
- ✅ Architecture de base pour les IA
- ✅ Tests unitaires pour les cartes
- ✅ Système de comparaison des cartes

### Fonctionnalités en cours de développement 🚧

- 🚧 Logique complète du jeu (Game.kt)
- 🚧 Implémentation des tours de jeu
- 🚧 Implémentation de RandomAi
- 🚧 Implémentation de EvaluateAi
- 🚧 Implémentation de MiniMaxAi
- 🚧 Gestion des plis et des tours
- 🚧 Interface utilisateur (CLI ou GUI)

### Fonctionnalités prévues 🔮

- 🔮 Mode multijoueur en ligne (RemoteHumanPlayer)
- 🔮 Interface graphique complète
- 🔮 Statistiques et historique des parties
- 🔮 Sauvegarde et chargement de parties
- 🔮 Configuration des règles du jeu
- 🔮 Mode tournoi
- 🔮 IA avancée avec apprentissage automatique

## 🧪 Tests

Le projet utilise JUnit 5 pour les tests unitaires. Les tests actuels couvrent :

- **CardTest.kt** : Tests pour la classe Card
  - Comparaison de cartes avec différents rangs
  - Comparaison de cartes avec le même rang
  - Format de la méthode toString()
  - Gestion des couleurs

Pour exécuter les tests avec un rapport détaillé :
```bash
./gradlew test --info
```

## 🤝 Contribution

Les contributions sont les bienvenues ! Si vous souhaitez contribuer :

1. Forkez le projet
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Poussez vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

### Standards de code

- Suivez les conventions Kotlin standard
- Documentez les nouvelles fonctionnalités avec des commentaires KDoc
- Ajoutez des tests unitaires pour les nouvelles fonctionnalités
- Assurez-vous que tous les tests passent avant de soumettre

## 📝 License

Ce projet est actuellement sans licence spécifiée. Veuillez contacter l'auteur pour plus d'informations sur l'utilisation.

## 👤 Auteur

**Maxime Bouju** - [@BoujuMaxime](https://github.com/BoujuMaxime)

## 📞 Contact

Pour toute question ou suggestion, n'hésitez pas à ouvrir une issue sur GitHub.

---

**Note** : Ce projet est en cours de développement actif. Les fonctionnalités et l'architecture peuvent évoluer.
