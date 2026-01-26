package view.themes

import javafx.scene.Scene
import javafx.scene.paint.Color

/**
 * Gestionnaire des thèmes CSS de l'application.
 */
class ThemeManager {

    private var currentTheme: String = "casino"

    val availableThemes: Map<String, String> = mapOf(
        "casino" to "🎰 Casino",
        "cartoon" to "🎨 Cartoon",
        "dark" to "🌙 Sombre Élégant",
        "minimal" to "✨ Minimaliste"
    )

    fun setTheme(themeName: String) {
        if (availableThemes.containsKey(themeName)) currentTheme = themeName
    }

    fun getCurrentTheme(): String = currentTheme

    fun applyTheme(scene: Scene) {
        scene.stylesheets.clear()

        javaClass.getResource("/themes/base.css")?.toExternalForm()?.let {
            scene.stylesheets.add(it)
        }

        if (currentTheme != "casino") {
            javaClass.getResource("/themes/$currentTheme.css")?.toExternalForm()?.let {
                scene.stylesheets.add(it)
            }
        }
    }

    fun getThemeColors(): ThemeColors = when (currentTheme) {
        "casino" -> ThemeColors(
            primary = Color.web("#1e4a35"),
            accent = Color.web("#ffd700"),
            text = Color.WHITE,
            cardRed = Color.web("#b71c1c"),
            cardBlack = Color.web("#1a1a1a")
        )
        "cartoon" -> ThemeColors(
            primary = Color.web("#667eea"),
            accent = Color.web("#ff6b6b"),
            text = Color.web("#2d3436"),
            cardRed = Color.web("#e17055"),
            cardBlack = Color.web("#2d3436")
        )
        "dark" -> ThemeColors(
            primary = Color.web("#1a1a2e"),
            accent = Color.web("#e94560"),
            text = Color.web("#eaeaea"),
            cardRed = Color.web("#ff6b6b"),
            cardBlack = Color.web("#d0d0d0")
        )
        "minimal" -> ThemeColors(
            primary = Color.web("#fafafa"),
            accent = Color.web("#2196F3"),
            text = Color.web("#212121"),
            cardRed = Color.web("#d32f2f"),
            cardBlack = Color.web("#212121")
        )
        else -> ThemeColors(
            primary = Color.web("#1e4a35"),
            accent = Color.web("#ffd700"),
            text = Color.WHITE,
            cardRed = Color.web("#b71c1c"),
            cardBlack = Color.web("#1a1a1a")
        )
    }
}

/**
 * Couleurs principales d'un thème.
 *
 * @property primary Couleur de fond principale
 * @property accent Couleur d'accent
 * @property text Couleur du texte
 * @property cardRed Couleur des cartes rouges
 * @property cardBlack Couleur des cartes noires
 */
data class ThemeColors(
    val primary: Color,
    val accent: Color,
    val text: Color,
    val cardRed: Color,
    val cardBlack: Color
)
