import model.Game
import model.player.HumanPlayer
import model.player.ai.EvaluateAi
import model.player.ai.RandomAi
import java.io.PrintStream

/**
 * Petit exécutable pour lancer et consulter deux parties de Président.
 */
fun main() {
    // Force la sortie console en UTF-8 (doit être placé en tout début)
    System.setOut(PrintStream(System.out, true, "UTF-8"))
    System.setErr(PrintStream(System.err, true, "UTF-8"))

    val game1 = Game(Game.GameParameters(nbPlayers = 4))
    game1.players.addAll(
        listOf(
            RandomAi("G1-P1", mutableListOf()),
            EvaluateAi("G1-P2", mutableListOf()),
            EvaluateAi("G1-P3", mutableListOf()),
            EvaluateAi("G1-P4", mutableListOf())
        )
    )

    // Lance et affiche chaque partie
    println("=== Lancement de la partie 1 ===")
    game1.startGame()
    printGameSummary(game1)

    println("\n=== Lancement de la partie 2 ===")
    game1.startGame()
    printGameSummary(game1)
}

private fun printGameSummary(game: Game) {
    println("Résumé de la partie :")
    game.players.forEach { player ->
        println("- ${player.id} : rôle=${player.role} | cartes=${player.hand.size} | main=${player.hand.joinToString()}")
    }
}