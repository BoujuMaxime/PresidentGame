# Interface Utilisateur JavaFX - Guide

## Vue d'ensemble

L'application PresidentGame dispose maintenant d'une interface graphique complète développée avec JavaFX 21, suivant une architecture MVC (Model-View-Controller).

## Lancement de l'application

Pour lancer l'interface graphique :

```bash
./gradlew run
```

## Fonctionnalités de l'interface

### Menu de configuration

L'application démarre avec un menu élégant permettant de configurer les paramètres de la partie :

#### Paramètres disponibles :
- **Nombre de joueurs** : Choisissez entre 3 et 6 joueurs
- **Difficulté des IA** : 
  - **Easy** : IA qui joue aléatoirement (RandomAi)
  - **Medium** : IA qui évalue les positions (EvaluateAi)
  - **Hard** : IA avancée (EvaluateAi optimisée)
- **Règles spéciales** :
  - **Carré Magique** : Compléter un carré de quatre cartes identiques remporte immédiatement le pli
  - **Ta Gueule (Force Play)** : Si la même carte est jouée deux fois de suite, les joueurs suivants doivent jouer cette même carte

### Plateau de jeu

Une fois la partie lancée, le plateau de jeu s'affiche avec les éléments suivants :

#### Informations des joueurs (en haut)
- Liste de tous les joueurs
- Rôle de chaque joueur (Président, Vice-Président, Neutre, Vice-Trou-du-Cul, Trou-du-Cul)
- Nombre de cartes restantes pour chaque joueur
- Indication visuelle du joueur actuel (en vert)

#### Centre du plateau
- **Message de jeu** : Indique le joueur actuel et les actions en cours
- **Dernier coup joué** : Affiche le dernier coup effectué avec le détail des cartes

#### Main du joueur (en bas)
- Affichage visuel de toutes vos cartes
- Cartes colorées selon leur couleur :
  - **Rouge** pour Cœur (♥) et Carreau (♦)
  - **Noir** pour Trèfle (♣) et Pique (♠)
- **Sélection interactive** :
  - Cliquez sur une carte pour la sélectionner (fond bleu)
  - Cliquez à nouveau pour la désélectionner
  - Sélectionnez plusieurs cartes du même rang pour jouer une paire, un brelan ou un carré

#### Boutons d'action
- **Jouer les cartes sélectionnées** : Valide votre coup avec les cartes sélectionnées
  - Vérifie automatiquement que les cartes forment une combinaison valide
  - Désactivé si aucune carte n'est sélectionnée ou si ce n'est pas votre tour
- **Passer mon tour** : Passe votre tour si vous ne pouvez ou ne voulez pas jouer
  - Activé uniquement pendant votre tour
- **Nouvelle partie** : Retourne au menu pour configurer une nouvelle partie

## Architecture technique

### Model-View-Controller (MVC)

#### Model (Modèle)
Les classes existantes dans le package `model/` :
- `Game` : Gère le déroulement complet d'une partie
- `GameTurns` : Orchestre les tours de jeu
- `Card` : Représente une carte avec son rang et sa couleur
- `PlayerMove` : Encapsule un coup joué (simple, paire, brelan, carré)
- `Player` et sous-classes : Représentent les joueurs (humain et IA)

#### View (Vue)
Package `view/` contenant les composants JavaFX :
- **`PresidentGameApp`** : Application JavaFX principale, point d'entrée
- **`MenuView`** : Interface du menu de configuration
  - ComboBox pour la sélection des paramètres
  - CheckBox pour les règles spéciales
  - Style moderne avec couleurs et effets hover
- **`GameBoardView`** : Interface du plateau de jeu
  - FlowPane pour l'affichage des cartes
  - Labels dynamiques pour les informations
  - Boutons d'action avec états (activé/désactivé)

#### Controller (Contrôleur)
Package `controller/` gérant la logique d'interaction :
- **`GameController`** : Contrôleur principal
  - Properties observables JavaFX pour la synchronisation avec la vue
  - Gestion du thread de jeu séparé de l'UI thread
  - Coordination entre le modèle et la vue
- **`GuiHumanPlayer`** : Adaptation du joueur humain pour JavaFX
  - Utilise `CompletableFuture` pour la communication asynchrone
  - Attend les actions de l'utilisateur dans l'interface
- **`ObservableAi`** : Wrapper pour les joueurs IA
  - Intercepte les actions des IA
  - Notifie le contrôleur pour mettre à jour l'interface
  - Ajoute des pauses pour que l'utilisateur puisse suivre le jeu

### Communication asynchrone

L'architecture utilise plusieurs mécanismes pour gérer l'asynchronisme :
- **Thread de jeu** : La partie se déroule dans un thread séparé pour ne pas bloquer l'interface
- **Platform.runLater()** : Mise à jour de l'interface depuis le thread de jeu
- **CompletableFuture** : Communication bidirectionnelle entre le contrôleur et le joueur humain
- **Properties observables** : Synchronisation automatique entre les données et l'affichage

## Structure des fichiers

```
src/main/kotlin/
├── Main.kt                          # Point d'entrée lançant l'application JavaFX
├── controller/
│   ├── GameController.kt            # Contrôleur principal
│   ├── GuiHumanPlayer.kt            # Joueur humain pour l'interface graphique
│   └── ObservableAi.kt              # Wrapper pour observer les IA
├── view/
│   ├── PresidentGameApp.kt          # Application JavaFX
│   ├── MenuView.kt                  # Menu de configuration
│   └── GameBoardView.kt             # Plateau de jeu
└── model/                            # Classes du modèle (inchangées)
    ├── Game.kt
    ├── GameTurns.kt
    ├── Card.kt
    ├── PlayerMove.kt
    └── player/
        ├── Player.kt
        ├── PlayerInterface.kt
        ├── HumanPlayer.kt           # Joueur console (pour tests)
        └── ai/
            ├── Ai.kt
            ├── RandomAi.kt
            ├── EvaluateAi.kt
            └── ...
```

## Dépendances

Les dépendances JavaFX ont été ajoutées dans `build.gradle.kts` :
- Plugin `org.openjfx.javafxplugin` version 0.1.0
- Modules JavaFX : `javafx.controls` et `javafx.fxml`
- Version JavaFX : 21 (compatible avec Java 21)

## Notes techniques

### Gestion des tours
1. Le joueur humain clique sur ses cartes et les boutons d'action
2. Le `GuiHumanPlayer` attend via un `CompletableFuture`
3. Quand l'action est soumise, le future est complété
4. Le thread de jeu continue avec le coup du joueur
5. Les IA jouent ensuite, notifiant l'interface à chaque coup

### Mise à jour de l'interface
Toutes les mises à jour de l'interface se font via `Platform.runLater()` pour garantir qu'elles s'exécutent sur le thread JavaFX Application Thread.

### Validation des coups
La validation des coups est faite à deux niveaux :
1. **Interface** : Vérifie que les cartes sélectionnées forment une combinaison valide (même rang)
2. **Modèle** : La classe `PlayerMove` valide la combinaison et vérifie si elle peut être jouée

## Améliorations possibles

- Ajouter des animations pour les cartes jouées
- Implémenter la sélection de cartes pour les échanges (actuellement automatique)
- Ajouter des sons et effets visuels
- Améliorer le style CSS des composants
- Ajouter un historique des coups joués
- Implémenter une vue résumé en fin de partie avec le classement final
