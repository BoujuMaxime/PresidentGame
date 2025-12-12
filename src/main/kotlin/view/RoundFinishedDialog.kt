package view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight

/**
 * Dialog affiché à la fin d'une manche pour proposer de continuer ou quitter.
 * 
 * @author BOUJU Maxime
 */
class RoundFinishedDialog(
    private val onNewRound: () -> Unit,
    private val onQuit: () -> Unit
) : VBox() {
    
    init {
        spacing = 20.0
        padding = Insets(30.0)
        alignment = Pos.CENTER
        style = """
            -fx-background-color: rgba(45, 52, 54, 0.95);
            -fx-background-radius: 10;
            -fx-border-color: #00b894;
            -fx-border-width: 2;
            -fx-border-radius: 10;
        """.trimIndent()
        
        minWidth = 400.0
        minHeight = 200.0
        
        // Titre
        val title = Label("Manche terminée !")
        title.font = Font.font("Arial", FontWeight.BOLD, 28.0)
        title.style = "-fx-text-fill: #dfe6e9;"
        
        // Message
        val message = Label("Les rôles ont été attribués.")
        message.font = Font.font("Arial", FontWeight.NORMAL, 16.0)
        message.style = "-fx-text-fill: #b2bec3;"
        
        val question = Label("Souhaitez-vous lancer une nouvelle manche ?")
        question.font = Font.font("Arial", FontWeight.NORMAL, 14.0)
        question.style = "-fx-text-fill: #b2bec3;"
        
        // Boutons
        val buttonBox = HBox(15.0)
        buttonBox.alignment = Pos.CENTER
        
        val newRoundButton = Button("Nouvelle Manche")
        newRoundButton.font = Font.font("Arial", FontWeight.BOLD, 14.0)
        newRoundButton.style = """
            -fx-background-color: #00b894;
            -fx-text-fill: white;
            -fx-padding: 12 30 12 30;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """.trimIndent()
        
        newRoundButton.setOnMouseEntered { 
            newRoundButton.style = """
                -fx-background-color: #00d9a8;
                -fx-text-fill: white;
                -fx-padding: 12 30 12 30;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
        }
        newRoundButton.setOnMouseExited { 
            newRoundButton.style = """
                -fx-background-color: #00b894;
                -fx-text-fill: white;
                -fx-padding: 12 30 12 30;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
        }
        
        newRoundButton.setOnAction { onNewRound() }
        
        val quitButton = Button("Quitter")
        quitButton.font = Font.font("Arial", FontWeight.BOLD, 14.0)
        quitButton.style = """
            -fx-background-color: #d63031;
            -fx-text-fill: white;
            -fx-padding: 12 30 12 30;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """.trimIndent()
        
        quitButton.setOnMouseEntered { 
            quitButton.style = """
                -fx-background-color: #ff7675;
                -fx-text-fill: white;
                -fx-padding: 12 30 12 30;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
        }
        quitButton.setOnMouseExited { 
            quitButton.style = """
                -fx-background-color: #d63031;
                -fx-text-fill: white;
                -fx-padding: 12 30 12 30;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
        }
        
        quitButton.setOnAction { onQuit() }
        
        buttonBox.children.addAll(newRoundButton, quitButton)
        
        // Ajouter tous les éléments
        children.addAll(title, message, question, buttonBox)
    }
}
