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
        style = "-fx-background-color: #2d3436;"
        
        // Titre
        val title = Label("Jeu du Président")
        title.font = Font.font("Arial", FontWeight.BOLD, 36.0)
        title.style = "-fx-text-fill: #dfe6e9;"
        
        val subtitle = Label("Configuration de la partie")
        subtitle.font = Font.font("Arial", FontWeight.NORMAL, 18.0)
        subtitle.style = "-fx-text-fill: #b2bec3;"
        
        // Grille de configuration
        val configGrid = GridPane()
        configGrid.hgap = 15.0
        configGrid.vgap = 15.0
        configGrid.alignment = Pos.CENTER
        configGrid.padding = Insets(20.0)
        configGrid.style = "-fx-background-color: #636e72; -fx-background-radius: 10;"
        
        // Nombre de joueurs
        val nbPlayersLabel = Label("Nombre de joueurs:")
        nbPlayersLabel.style = "-fx-text-fill: #dfe6e9; -fx-font-size: 14px;"
        nbPlayersComboBox = ComboBox()
        nbPlayersComboBox.items.addAll(3, 4, 5, 6)
        nbPlayersComboBox.value = 4
        nbPlayersComboBox.style = "-fx-font-size: 14px;"
        configGrid.add(nbPlayersLabel, 0, 0)
        configGrid.add(nbPlayersComboBox, 1, 0)
        
        // Difficulté des IA
        val difficultyLabel = Label("Difficulté des IA:")
        difficultyLabel.style = "-fx-text-fill: #dfe6e9; -fx-font-size: 14px;"
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
        val rulesLabel = Label("Règles spéciales:")
        rulesLabel.style = "-fx-text-fill: #dfe6e9; -fx-font-size: 14px; -fx-font-weight: bold;"
        configGrid.add(rulesLabel, 0, 2)
        
        carreMagiqueCheckBox = CheckBox("Carré Magique")
        carreMagiqueCheckBox.isSelected = true
        carreMagiqueCheckBox.style = "-fx-text-fill: #dfe6e9; -fx-font-size: 13px;"
        configGrid.add(carreMagiqueCheckBox, 1, 2)
        
        taGueuleCheckBox = CheckBox("Ta Gueule (Force Play)")
        taGueuleCheckBox.isSelected = true
        taGueuleCheckBox.style = "-fx-text-fill: #dfe6e9; -fx-font-size: 13px;"
        configGrid.add(taGueuleCheckBox, 1, 3)
        
        // Bouton démarrer
        startButton = Button("Démarrer la partie")
        startButton.font = Font.font("Arial", FontWeight.BOLD, 16.0)
        startButton.style = """
            -fx-background-color: #00b894;
            -fx-text-fill: white;
            -fx-padding: 15 40 15 40;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """.trimIndent()
        
        startButton.setOnMouseEntered { 
            startButton.style = """
                -fx-background-color: #00d9a8;
                -fx-text-fill: white;
                -fx-padding: 15 40 15 40;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
        }
        startButton.setOnMouseExited { 
            startButton.style = """
                -fx-background-color: #00b894;
                -fx-text-fill: white;
                -fx-padding: 15 40 15 40;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
        }
        
        startButton.setOnAction {
            handleStartGame()
        }
        
        // Ajouter tous les éléments
        children.addAll(title, subtitle, configGrid, startButton)
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
