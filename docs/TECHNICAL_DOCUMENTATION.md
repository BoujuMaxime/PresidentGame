# 🛠️ Documentation Technique - Interface Graphique

## Vue d'ensemble de l'Architecture

L'interface graphique du jeu du Président est construite sur **JavaFX** avec une architecture **MVC** (Modèle-Vue-Contrôleur) et un système de **thèmes CSS modulaire**.

```
┌─────────────────────────────────────────────────────────────┐
│                      COUCHE VUE                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │PresidentGame│  │  MenuView   │  │   GameBoardView     │  │
│  │    App      │──│             │──│                     │  │
│  │(Navigation) │  │  (Config)   │  │  (Plateau de jeu)   │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│         │                                    │              │
│  ┌──────┴────────────────────────────────────┴───────────┐  │
│  │                    COMPOSANTS                         │  │
│  │  CardView │ PlayerPanel │ PileView │ EventLog │ ...   │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           │
                           │ Propriétés Observables
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    COUCHE CONTRÔLEUR                        │
│  ┌────────────────────────────────────────────────────────┐ │
│  │               GameController                           │ │
│  │  - gameStateProperty      - humanPlayerHandProperty    │ │
│  │  - currentPlayerProperty  - lastPlayedMoveProperty     │ │
│  │  - canPlayProperty        - playersInfoProperty        │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌─────────────────┐  ┌──────────────────────────────────┐  │
│  │  GuiHumanPlayer │  │         ObservableAi             │  │
│  │  (Future-based) │  │  (Wrapper avec notifications)    │  │
│  └─────────────────┘  └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           │
                           │ Appels directs
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      COUCHE MODÈLE                          │
│  Game │ GameTurns │ Card │ PlayerMove │ Player │ AI         │
└─────────────────────────────────────────────────────────────┘
```

---

## Composants Principaux

### 1. PresidentGameApp

**Fichier** : `view/PresidentGameApp.kt`

Point d'entrée de l'application JavaFX. Gère :
- L'initialisation de la scène et du stage
- La navigation entre les vues (menu ↔ plateau)
- L'application des thèmes

```kotlin
class PresidentGameApp : Application() {
    private lateinit var stage: Stage
    private lateinit var rootPane: StackPane
    private lateinit var controller: GameController
    private lateinit var themeManager: ThemeManager
    
    override fun start(primaryStage: Stage) {
        // Initialisation...
        showMenu() // Vue par défaut
    }
    
    fun showMenu() { /* Affiche MenuView */ }
    private fun startGame(config: GameConfig) { /* Lance GameBoardView */ }
    private fun changeTheme(themeName: String) { /* Change le thème CSS */ }
}
```

**Points clés** :
- `rootPane` est un `StackPane` qui contient la vue active
- Le `GameController` est réutilisé entre les parties
- `ThemeManager` est appliqué à la scène entière

---

### 2. MenuView

**Fichier** : `view/MenuView.kt`

Écran de configuration avec :
- Slider pour le nombre de joueurs (3-6)
- ComboBox pour la difficulté IA
- CheckBox pour les règles spéciales
- ComboBox pour le thème
- Animation de fond avec cartes flottantes

```kotlin
class MenuView(
    private val onStartGame: (GameConfig) -> Unit,
    private val onThemeChange: (String) -> Unit,
    private val themeManager: ThemeManager
) : StackPane() {
    
    private fun createAnimatedBackground(): Pane { /* Cartes flottantes */ }
    private fun createMenuPanel(): VBox { /* Panneau de configuration */ }
    private fun startGame() { /* Appelle onStartGame avec la config */ }
}
```

**Callbacks** :
- `onStartGame(GameConfig)` : Appelé quand l'utilisateur clique "Démarrer"
- `onThemeChange(String)` : Appelé quand le thème change dans le ComboBox

---

### 3. GameBoardView

**Fichier** : `view/GameBoardView.kt`

Vue principale du plateau de jeu. Hérite de `BorderPane` :

```
┌─────────────────────────────────────────────────────────┐
│ TOP: Menu button │ Adversaires (PlayerPanel) │ Status   │
├─────────────────────────────────────────────────────────┤
│                                           │             │
│         CENTER: Pile + Messages           │  EventLog   │
│                                           │             │
├─────────────────────────────────────────────────────────┤
│ BOTTOM: Info joueur │ PlayerHandView │ ActionBar        │
└─────────────────────────────────────────────────────────┘
```

**Bindings avec le contrôleur** :
```kotlin
controller.humanPlayerHandProperty.addListener { _, _, newHand ->
    playerHandView.updateHand(newHand)
}

controller.currentPileProperty.addListener { _, _, newPile ->
    pileView.updatePile(newPile)
}

controller.canPlayProperty.addListener { _, _, canPlay ->
    playerHandView.setInteractive(canPlay)
}
```

**Dialogues** :
- `showInGameMenu()` : Menu pause
- `showRoundFinishedDialog()` : Fin de manche
- `showCardExchangeDialog(count, highest)` : Échange de cartes

---

## Composants Réutilisables

### CardView

**Fichier** : `view/components/CardView.kt`

Représentation visuelle d'une carte.

```kotlin
class CardView(
    val card: Card,
    private val onSelect: ((CardView) -> Unit)? = null
) : StackPane() {
    
    fun setSelected(selected: Boolean)  // Change l'apparence
    fun isSelected(): Boolean           // État de sélection
    fun setInteractive(interactive: Boolean) // Active/désactive
    fun playCardAnimation(targetX, targetY, onFinished) // Animation
}
```

**Classes CSS** :
- `.card-container` : Style normal
- `.card-container-selected` : Quand sélectionnée
- `.card-red` / `.card-black` : Couleur du texte

---

### PlayerPanel

**Fichier** : `view/components/PlayerPanel.kt`

Panneau d'un joueur adverse.

```kotlin
class PlayerPanel(
    playerName: String,
    role: Player.Role,
    cardCount: Int,
    isCurrentPlayer: Boolean
) : VBox() {
    
    fun update(newRole, newCardCount, newIsCurrentPlayer)
    fun setHighlighted(highlighted: Boolean) // Animation pulsation
    fun showMessage(message: String, duration: Double) // Bulle
}
```

**Affichage** :
- Nom du joueur
- Badge de rôle (Président, Vice-Président, etc.)
- Cartes dos (CardBackView)
- Compteur de cartes

---

### PileView

**Fichier** : `view/components/PileView.kt`

Zone centrale où les cartes sont jouées.

```kotlin
class PileView : StackPane() {
    fun updatePile(cards: List<Card>)    // Met à jour l'affichage
    fun playVictoryAnimation()           // Animation spéciale
}
```

**Logique d'affichage** :
- Affiche uniquement les cartes du dernier coup (même rang)
- Cartes légèrement chevauchées avec rotation aléatoire
- Animation de fade lors des mises à jour

---

### PlayerHandView

**Fichier** : `view/components/PlayerHandView.kt`

Main du joueur humain avec sélection.

```kotlin
class PlayerHandView(
    private val onSelectionChanged: () -> Unit
) : HBox() {
    
    fun updateHand(cards: List<Card>)      // Remplace toutes les cartes
    fun getSelectedCards(): List<Card>      // Cartes sélectionnées
    fun clearSelection()                    // Désélectionne tout
    fun setInteractive(interactive: Boolean) // Active/désactive
    fun selectCards(cards: List<Card>)      // Sélection programmatique
}
```

**Règle de sélection** :
- On ne peut sélectionner que des cartes de même rang
- Si on clique sur un rang différent, la sélection précédente est effacée

---

### EventLog

**Fichier** : `view/components/EventLog.kt`

Journal scrollable des événements.

```kotlin
class EventLog : VBox() {
    enum class EventType(val icon: String, val styleClass: String) {
        INFO("ℹ️", "event-info"),
        PLAYER_ACTION("🎴", "event-player"),
        AI_ACTION("🤖", "event-ai"),
        // ...
    }
    
    fun addEvent(message: String, type: EventType)
    fun clear()
    fun addSeparator(title: String)
}
```

**Fonctionnalités** :
- Auto-scroll vers le bas
- Limite de 50 entrées (les anciennes sont supprimées)
- Animation de fade-in pour les nouvelles entrées

---

## Système de Thèmes

### ThemeManager

**Fichier** : `view/themes/ThemeManager.kt`

```kotlin
class ThemeManager {
    val availableThemes: Map<String, String> = mapOf(
        "casino" to "🎰 Casino",
        "cartoon" to "🎨 Cartoon",
        "dark" to "🌙 Sombre Élégant",
        "minimal" to "✨ Minimaliste"
    )
    
    fun setTheme(themeName: String)      // Change le thème
    fun getCurrentTheme(): String         // Thème actuel
    fun applyTheme(scene: Scene)          // Applique les CSS
    fun getThemeColors(): ThemeColors     // Couleurs pour usage programmatique
}
```

### Structure CSS

```
themes/
├── base.css      # Tous les styles de base (thème Casino)
├── casino.css    # Vide (utilise base.css)
├── cartoon.css   # Surcharges pour Cartoon
├── dark.css      # Surcharges pour Dark
└── minimal.css   # Surcharges pour Minimal
```

**Ordre de chargement** :
1. `base.css` est toujours chargé en premier
2. `{theme}.css` est chargé ensuite pour surcharger

### Classes CSS Importantes

| Classe | Usage |
|--------|-------|
| `.game-board` | Fond du plateau |
| `.menu-panel` | Conteneur du menu |
| `.card-container` | Carte normale |
| `.card-container-selected` | Carte sélectionnée |
| `.player-box` | Panneau adversaire |
| `.player-box-highlighted` | Adversaire actif |
| `.pile-stack` | Zone de pile |
| `.action-button` | Tous les boutons |
| `.play-button` | Bouton Jouer (vert) |
| `.pass-button` | Bouton Passer (rouge) |
| `.event-entry` | Entrée du journal |

---

## Communication Vue ↔ Contrôleur

### Propriétés Observables

Le `GameController` expose des `Property` JavaFX :

```kotlin
// Dans GameController
val humanPlayerHandProperty = SimpleObjectProperty<List<Card>>(emptyList())
val currentPileProperty = SimpleObjectProperty<List<Card>>(emptyList())
val canPlayProperty = SimpleBooleanProperty(false)
val gameMessageProperty = SimpleStringProperty("")
// ...
```

La vue s'abonne à ces propriétés :

```kotlin
// Dans GameBoardView
controller.humanPlayerHandProperty.addListener { _, _, newHand ->
    Platform.runLater {
        playerHandView.updateHand(newHand ?: emptyList())
    }
}
```

### CompletableFuture pour le Tour Humain

Quand c'est au joueur humain de jouer :

1. **Contrôleur** appelle `notifyHumanPlayerTurn()` qui retourne un `CompletableFuture`
2. **Vue** active les contrôles et attend l'action utilisateur
3. **Utilisateur** sélectionne des cartes et clique "Jouer"
4. **Vue** appelle `controller.submitPlayerMove(move)`
5. **GuiHumanPlayer** complète le `Future` avec le coup
6. **Modèle** continue l'exécution

```kotlin
// Dans GuiHumanPlayer
fun notifyHumanPlayerTurn(...): CompletableFuture<PlayerMove?> {
    val future = CompletableFuture<PlayerMove?>()
    moveFuture = future
    // Activer l'UI...
    return future
}

fun submitMove(move: PlayerMove?) {
    moveFuture?.complete(move)
    moveFuture = null
}
```

---

## Bonnes Pratiques

### Thread Safety

Toujours utiliser `Platform.runLater` pour les modifications UI depuis un autre thread :

```kotlin
// ❌ Mauvais - peut causer des exceptions
controller.humanPlayerHandProperty.set(newHand)

// ✅ Bon - s'exécute sur le thread UI
Platform.runLater {
    controller.humanPlayerHandProperty.set(newHand)
}
```

### Gestion Mémoire

Éviter les fuites mémoire avec les listeners :

```kotlin
// Stocker la référence du listener
private val handListener = ChangeListener<List<Card>> { _, _, new -> ... }

// L'ajouter
controller.humanPlayerHandProperty.addListener(handListener)

// Le retirer quand plus nécessaire (ex: dans stop())
controller.humanPlayerHandProperty.removeListener(handListener)
```

### Style CSS

Préférer les classes CSS aux styles inline :

```kotlin
// ❌ Mauvais
label.style = "-fx-text-fill: red; -fx-font-size: 14px;"

// ✅ Bon
label.styleClass.add("error-label")
// Définir .error-label dans le CSS
```

---

## Tests

### Structure des Tests

```
src/test/kotlin/
├── controller/
│   └── GuiHumanPlayerTest.kt  # Tests du joueur GUI
└── model/
    └── ...                     # Tests existants
```

### Tester les Composants UI

Pour tester les composants JavaFX, utiliser **TestFX** :

```kotlin
// Exemple de test
@Test
fun `card selection should work`() {
    val cardView = CardView(Card(Card.Rank.ACE, Card.Suit.HEARTS))
    
    assertFalse(cardView.isSelected())
    
    cardView.setSelected(true)
    
    assertTrue(cardView.isSelected())
    assertTrue(cardView.styleClass.contains("card-container-selected"))
}
```

---

## Dépannage

### CSS non appliqué

1. Vérifier que le fichier est dans `src/main/resources/themes/`
2. Vérifier les erreurs CSS dans la console (`WARNING: CSS Error...`)
3. Vérifier que la classe est bien ajoutée au nœud (`node.styleClass.add("...")`)

### Animation ne joue pas

1. Vérifier que le nœud est visible et dans le graphe de scène
2. Vérifier que `.play()` est appelé
3. Vérifier que l'animation n'est pas arrêtée par une autre

### Binding ne se met pas à jour

1. Vérifier que `Platform.runLater` est utilisé
2. Vérifier que la propriété n'est pas remplacée par une nouvelle instance
3. Vérifier que le listener est toujours attaché

---

*Documentation technique - PresidentGame v1.0-SNAPSHOT*
