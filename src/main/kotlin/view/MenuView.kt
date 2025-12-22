package view

import controller.GameController
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import model.Game

/**
 * Vue du menu de configuration de la partie.
 * Permet de configurer les paramètres avant de démarrer une partie.
 * 
 * @author BOUJU Maxime
 */
class MenuView(private val controller: GameController, private val onStartGame: () -> Unit) : VBox() {
    
    private val nbPlayersComboBox: ComboBox<Int>
    private val carreMagiqueCheckBox: CheckBox
    private val taGueuleCheckBox: CheckBox
    private val difficultyComboBox: ComboBox<Game.GameParameters.DifficultyLevel>
    private val startButton: Button
    
    init {
        spacing = 20.0
        padding = Insets(30.0)
        alignment = Pos.CENTER
        style = """
            -fx-background-color: radial-gradient(center 50% 50%, radius 100%, 
                #1e4a35 0%, 
                #0d2818 60%,
                #061208 100%);
        """.trimIndent()
        
        // Icône et Titre
        val iconLabel = Label("🃏")
        iconLabel.font = Font.font("Georgia", FontWeight.BOLD, 64.0)
        iconLabel.style = "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 10, 0.0, 0, 4);"
        
        val title = Label("Jeu du Président")
        title.font = Font.font("Georgia", FontWeight.BOLD, 46.0)
        title.style = """
            -fx-text-fill: #ffd700;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.8), 8, 0.0, 0, 4);
        """.trimIndent()
        
        val subtitle = Label("Configuration de la partie")
        subtitle.font = Font.font("Georgia", FontWeight.NORMAL, 18.0)
        subtitle.style = "-fx-text-fill: #c8e6c9;"
        
        // Grille de configuration
        val configGrid = GridPane()
        configGrid.hgap = 15.0
        configGrid.vgap = 15.0
        configGrid.alignment = Pos.CENTER
        configGrid.padding = Insets(25.0)
        configGrid.style = """
            -fx-background-color: linear-gradient(to bottom, 
                rgba(30, 74, 53, 0.95) 0%, 
                rgba(20, 50, 35, 0.98) 100%);
            -fx-background-radius: 18;
            -fx-border-color: linear-gradient(to bottom, #6b4c35 0%, #4a3728 50%, #3d2a1c 100%);
            -fx-border-width: 4;
            -fx-border-radius: 18;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 20, 0.0, 0, 6);
        """.trimIndent()
        
        // Nombre de joueurs
        val nbPlayersLabel = Label("👥 Nombre de joueurs:")
        nbPlayersLabel.style = "-fx-text-fill: #ffd700; -fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold;"
        nbPlayersComboBox = ComboBox()
        nbPlayersComboBox.items.addAll(3, 4, 5, 6)
        nbPlayersComboBox.value = 4
        nbPlayersComboBox.style = "-fx-font-size: 14px;"
        configGrid.add(nbPlayersLabel, 0, 0)
        configGrid.add(nbPlayersComboBox, 1, 0)
        
        // Difficulté des IA
        val difficultyLabel = Label("🤖 Difficulté des IA:")
        difficultyLabel.style = "-fx-text-fill: #ffd700; -fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold;"
        difficultyComboBox = ComboBox()
        difficultyComboBox.items.addAll(
            Game.GameParameters.DifficultyLevel.EASY,
            Game.GameParameters.DifficultyLevel.MEDIUM,
            Game.GameParameters.DifficultyLevel.HARD
        )
        difficultyComboBox.value = Game.GameParameters.DifficultyLevel.MEDIUM
        difficultyComboBox.style = "-fx-font-size: 14px;"
        configGrid.add(difficultyLabel, 0, 1)
        configGrid.add(difficultyComboBox, 1, 1)
        
        // Règles spéciales
        val rulesLabel = Label("⚙️ Règles spéciales:")
        rulesLabel.style = "-fx-text-fill: #ffd700; -fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold;"
        configGrid.add(rulesLabel, 0, 2)
        
        carreMagiqueCheckBox = CheckBox("✨ Carré Magique")
        carreMagiqueCheckBox.isSelected = true
        carreMagiqueCheckBox.style = "-fx-text-fill: #c8e6c9; -fx-font-family: 'Georgia'; -fx-font-size: 14px;"
        configGrid.add(carreMagiqueCheckBox, 1, 2)
        
        taGueuleCheckBox = CheckBox("🔇 Ta Gueule (Force Play)")
        taGueuleCheckBox.isSelected = true
        taGueuleCheckBox.style = "-fx-text-fill: #c8e6c9; -fx-font-family: 'Georgia'; -fx-font-size: 14px;"
        configGrid.add(taGueuleCheckBox, 1, 3)
        
        // Bouton démarrer
        startButton = Button("🎮 Démarrer la partie")
        startButton.font = Font.font("Georgia", FontWeight.BOLD, 18.0)
        startButton.style = """
            -fx-background-color: linear-gradient(to bottom, #2ecc71 0%, #27ae60 50%, #1e8449 100%);
            -fx-text-fill: white;
            -fx-padding: 18 50 18 50;
            -fx-background-radius: 12;
            -fx-border-color: #1a5c36;
            -fx-border-width: 3;
            -fx-border-radius: 12;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 12, 0.0, 0, 5);
        """.trimIndent()
        
        startButton.setOnMouseEntered { 
            startButton.style = """
                -fx-background-color: linear-gradient(to bottom, #58d68d 0%, #2ecc71 50%, #27ae60 100%);
                -fx-text-fill: white;
                -fx-padding: 18 50 18 50;
                -fx-background-radius: 12;
                -fx-border-color: #1a5c36;
                -fx-border-width: 3;
                -fx-border-radius: 12;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(46, 204, 113, 0.8), 18, 0.0, 0, 6);
            """.trimIndent()
        }
        startButton.setOnMouseExited { 
            startButton.style = """
                -fx-background-color: linear-gradient(to bottom, #2ecc71 0%, #27ae60 50%, #1e8449 100%);
                -fx-text-fill: white;
                -fx-padding: 18 50 18 50;
                -fx-background-radius: 12;
                -fx-border-color: #1a5c36;
                -fx-border-width: 3;
                -fx-border-radius: 12;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 12, 0.0, 0, 5);
            """.trimIndent()
        }
        
        startButton.setOnAction {
            handleStartGame()
        }
        
        // Ajouter tous les éléments
        children.addAll(iconLabel, title, subtitle, configGrid, startButton)
    }
    
    /**
     * Gère le démarrage de la partie avec les paramètres configurés.
     */
    private fun handleStartGame() {
        val nbPlayers = nbPlayersComboBox.value
        val withCarreMagique = carreMagiqueCheckBox.isSelected
        val withTaGueule = taGueuleCheckBox.isSelected
        val difficulty = difficultyComboBox.value
        
        controller.startNewGame(nbPlayers, withCarreMagique, withTaGueule, difficulty)
        onStartGame()
    }
}
