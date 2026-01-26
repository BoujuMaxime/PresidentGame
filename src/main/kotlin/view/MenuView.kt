package view

import javafx.animation.*
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import model.Game
import view.themes.ThemeManager

/**
 * Vue du menu principal avec configuration de partie.
 *
 * Permet de configurer le nombre de joueurs, la difficulté,
 * les règles spéciales et le thème visuel avant de lancer une partie.
 *
 * @property onStartGame Callback appelé avec la configuration au démarrage
 * @property onThemeChange Callback appelé lors du changement de thème
 * @property themeManager Gestionnaire de thèmes pour lister les options
 * @property playerCountSlider Slider de sélection du nombre de joueurs
 * @property playerCountLabel Label affichant le nombre de joueurs
 * @property difficultyCombo ComboBox de sélection de la difficulté
 * @property carreMagiqueCheck CheckBox pour la règle Carré Magique
 * @property taGueuleCheck CheckBox pour la règle Ta Gueule
 * @property themeCombo ComboBox de sélection du thème
 *
 * @author BOUJU Maxime
 */
class MenuView(
    private val onStartGame: (GameConfig) -> Unit,
    private val onThemeChange: (String) -> Unit,
    private val themeManager: ThemeManager
) : StackPane() {

    private val playerCountSlider = Slider(3.0, 6.0, 4.0)
    private val playerCountLabel = Label("4 joueurs")
    private val difficultyCombo = ComboBox<String>()
    private val carreMagiqueCheck = CheckBox("Carré Magique")
    private val taGueuleCheck = CheckBox("Ta Gueule")
    private val themeCombo = ComboBox<String>()

    init {
        styleClass.add("menu-root")
        setupUI()
    }

    /**
     * Configure l'interface du menu.
     */
    private fun setupUI() {
        children.addAll(createAnimatedBackground(), createMenuPanel())
    }

    /**
     * Crée le fond animé avec cartes flottantes.
     *
     * @return Pane contenant les cartes animées
     */
    private fun createAnimatedBackground(): Pane = Pane().apply {
        styleClass.add("menu-background")
        val cardSymbols = listOf("♠", "♥", "♦", "♣")
        val colors = listOf(Color.web("#1a1a1a80"), Color.web("#b71c1c80"), Color.web("#b71c1c80"), Color.web("#1a1a1a80"))

        repeat(12) { i ->
            val card = createFloatingCard(cardSymbols[i % 4], colors[i % 4]).apply {
                layoutX = (i * 120.0) % 1200
                layoutY = (i * 80.0) % 700
            }

            TranslateTransition(Duration.seconds(3.0 + (i % 3)), card).apply {
                byY = 30.0
                cycleCount = Animation.INDEFINITE
                isAutoReverse = true
                delay = Duration.millis((i * 200).toDouble())
            }.play()

            RotateTransition(Duration.seconds(8.0 + (i % 4)), card).apply {
                byAngle = if (i % 2 == 0) 10.0 else -10.0
                cycleCount = Animation.INDEFINITE
                isAutoReverse = true
            }.play()

            children.add(card)
        }
    }

    /**
     * Crée une carte flottante décorative.
     *
     * @param symbol Symbole de la carte (♠, ♥, ♦, ♣)
     * @param color Couleur du symbole
     * @return StackPane représentant la carte
     */
    private fun createFloatingCard(symbol: String, color: Color): StackPane = StackPane().apply {
        opacity = 0.3
        children.addAll(
            Rectangle(60.0, 90.0).apply {
                fill = Color.web("#fffef5")
                arcWidth = 10.0
                arcHeight = 10.0
                stroke = Color.web("#c9c4a880")
                strokeWidth = 2.0
            },
            Label(symbol).apply {
                style = "-fx-font-size: 36px; -fx-text-fill: ${toHexString(color)};"
            }
        )
    }

    /**
     * Convertit une couleur en chaîne hexadécimale.
     *
     * @param color Couleur à convertir
     * @return Chaîne au format #RRGGBB
     */
    private fun toHexString(color: Color): String = String.format("#%02X%02X%02X",
        (color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt())

    /**
     * Crée le panneau principal du menu.
     *
     * @return VBox contenant tous les éléments de configuration
     */
    private fun createMenuPanel(): VBox = VBox(20.0).apply {
        styleClass.add("menu-panel")
        alignment = Pos.CENTER
        maxWidth = 500.0
        padding = Insets(40.0)

        children.addAll(
            Label("🃏 Jeu du Président").apply { styleClass.add("menu-title") },
            Label("Le classique des jeux de cartes").apply { styleClass.add("menu-subtitle") },
            Separator().apply { padding = Insets(10.0, 0.0, 10.0, 0.0) },
            createConfigSection(),
            createRulesSection(),
            createThemeSection(),
            Separator().apply { padding = Insets(10.0, 0.0, 10.0, 0.0) },
            Button("🎮 Démarrer la partie").apply {
                styleClass.addAll("action-button", "start-button")
                maxWidth = Double.MAX_VALUE
                setOnAction { startGame() }
            },
            createRulesInfoPanel()
        )
    }

    /**
     * Crée la section de configuration joueurs et difficulté.
     *
     * @return VBox avec slider joueurs et combo difficulté
     */
    private fun createConfigSection(): VBox = VBox(15.0).apply {
        styleClass.add("config-section")

        playerCountSlider.apply {
            styleClass.add("config-slider")
            isShowTickLabels = true
            isShowTickMarks = true
            majorTickUnit = 1.0
            minorTickCount = 0
            isSnapToTicks = true
            blockIncrement = 1.0
            valueProperty().addListener { _, _, newVal -> playerCountLabel.text = "${newVal.toInt()} joueurs" }
        }

        difficultyCombo.apply {
            styleClass.add("config-combo")
            items.addAll("Facile", "Moyen", "Difficile")
            value = "Moyen"
            maxWidth = Double.MAX_VALUE
        }

        children.addAll(
            VBox(5.0).apply {
                children.addAll(
                    Label("👥 Nombre de joueurs").apply { styleClass.add("config-label") },
                    HBox(15.0).apply {
                        alignment = Pos.CENTER_LEFT
                        children.addAll(playerCountSlider, playerCountLabel.apply {
                            styleClass.add("config-value-label")
                            minWidth = 80.0
                        })
                    }
                )
            },
            VBox(5.0).apply {
                children.addAll(
                    Label("🤖 Difficulté des IA").apply { styleClass.add("config-label") },
                    difficultyCombo
                )
            }
        )
    }

    /**
     * Crée la section des règles spéciales.
     *
     * @return VBox avec les checkboxes des règles
     */
    private fun createRulesSection(): VBox = VBox(10.0).apply {
        styleClass.add("rules-section")

        carreMagiqueCheck.apply {
            styleClass.add("config-checkbox")
            isSelected = true
            tooltip = Tooltip("Un carré (4 cartes identiques) termine le pli et celui qui le pose rejoue")
        }

        taGueuleCheck.apply {
            styleClass.add("config-checkbox")
            isSelected = true
            tooltip = Tooltip("Si 2 joueurs consécutifs jouent la même valeur, le joueur suivant doit jouer cette valeur ou passer")
        }

        children.addAll(
            Label("📜 Règles spéciales").apply { styleClass.add("config-label") },
            carreMagiqueCheck,
            taGueuleCheck
        )
    }

    /**
     * Crée la section de sélection du thème.
     *
     * @return VBox avec le combo des thèmes
     */
    private fun createThemeSection(): VBox = VBox(10.0).apply {
        styleClass.add("theme-section")

        themeCombo.apply {
            styleClass.add("config-combo")
            items.addAll(themeManager.availableThemes.values)
            value = themeManager.availableThemes[themeManager.getCurrentTheme()]
            maxWidth = Double.MAX_VALUE
            valueProperty().addListener { _, _, newVal ->
                themeManager.availableThemes.entries.find { it.value == newVal }?.key?.let { onThemeChange(it) }
            }
        }

        children.addAll(
            Label("🎨 Thème visuel").apply { styleClass.add("config-label") },
            themeCombo
        )
    }

    /**
     * Crée le panneau d'information sur les règles.
     *
     * @return VBox avec les règles de base
     */
    private fun createRulesInfoPanel(): VBox = VBox(8.0).apply {
        styleClass.add("rules-info-panel")
        padding = Insets(15.0)

        children.addAll(
            Label("ℹ️ Règles du jeu").apply { styleClass.add("rules-info-title") },
            Label("""
                • Le but est de se débarrasser de toutes ses cartes
                • Jouez des cartes de même valeur (1, 2, 3 ou 4 cartes)
                • La combinaison doit être supérieure ou égale à la précédente
                • Le dernier à poser des cartes remporte le pli
                • Premier à finir = Président, Dernier = Trou du Cul
            """.trimIndent()).apply {
                styleClass.add("rules-info-text")
                isWrapText = true
            }
        )
    }

    /**
     * Démarre la partie avec la configuration actuelle.
     */
    private fun startGame() {
        val difficulty = when (difficultyCombo.value) {
            "Facile" -> Game.GameParameters.DifficultyLevel.EASY
            "Difficile" -> Game.GameParameters.DifficultyLevel.HARD
            else -> Game.GameParameters.DifficultyLevel.MEDIUM
        }

        onStartGame(GameConfig(
            playerCount = playerCountSlider.value.toInt(),
            difficulty = difficulty,
            withCarreMagique = carreMagiqueCheck.isSelected,
            withTaGueule = taGueuleCheck.isSelected
        ))
    }
}
