package model.player

import model.Card
import model.PlayerMove

/**
 * Représente un joueur humain dans le jeu.
 *
 * @param id L'identifiant unique du joueur.
 * @param hand La main initiale du joueur, une liste mutable de cartes.
 */
class HumanPlayer(
    id: String,
    hand: MutableList<Card>
) : Player(id, hand) {

    /**
     * Affiche la main du joueur ainsi que les coups possibles.
     *
     * @param possibleMoves Une liste des coups possibles que le joueur peut effectuer.
     */
    private fun displayHand(possibleMoves: List<PlayerMove>) {
        PlayerUtils.printHand(hand) // Affiche les cartes dans la main.
        println("Coups possibles :")
        if (possibleMoves.isEmpty()) {
            println("  Aucun coup valide") // Message si aucun coup n'est possible.
        } else {
            possibleMoves.forEachIndexed { index, move ->
                println("  $index -> $move") // Affiche chaque coup possible avec son indice.
            }
        }
    }

    /**
     * Permet au joueur humain de jouer son tour.
     *
     * @param pile La pile principale de cartes.
     * @param discardPile La pile de défausse.
     * @param lastPlayerMove Le dernier coup joué par un autre joueur, ou null si aucun.
     * @param straightRank Le rang de la séquence en cours, ou null si aucune séquence.
     * @return Le coup choisi par le joueur, ou null si le joueur passe son tour.
     */
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        val possibleMoves = PlayerUtils.possiblePlays(hand, lastPlayerMove, straightRank)
        displayHand(possibleMoves) // Affiche la main et les coups possibles.
        println("Sélectionnez l'indice du coup à jouer ou appuyez sur Entrée pour passer :")
        while (true) {
            val input = readlnOrNull()?.trim() // Lit l'entrée utilisateur.
            val index = input?.toIntOrNull()
            if (input.isNullOrEmpty()) return null // Retourne null si l'utilisateur passe son tour.
            if (index != null && index in possibleMoves.indices) return possibleMoves[index] // Retourne le coup choisi.
            println("Indice invalide, recommencez ou appuyez sur Entrée pour passer.") // Message d'erreur pour une entrée invalide.
        }
    }

    /**
     * Permet de choisir des cartes à échanger avec un autre joueur.
     *
     * @param count Le nombre de cartes à échanger.
     * @param highest Si `true`, sélectionne les cartes les plus fortes, sinon il choisit.
     * @return La liste des cartes sélectionnées pour l'échange.
     */
    override fun exchangeCard(count: Int, highest: Boolean): List<Card> {
        if (count <= 0 || hand.isEmpty()) return emptyList()

        if (highest) {
            val picked = PlayerUtils.selectableCardsForExchange(hand, count, true).take(count)
            return picked
        }

        println("Sélectionnez $count carte(s) à échanger (indices séparés par des espaces).")
        PlayerUtils.printHand(hand)
        println("Appuyez sur Entrée pour sélectionner automatiquement les $count premières cartes.")

        while (true) {
            val input = readlnOrNull()?.trim()
            if (input.isNullOrEmpty()) {
                val defaultPick = hand.take(count)
                return defaultPick
            }

            val indices = input.split(Regex("\\s+"))
                .mapNotNull { it.toIntOrNull() }
            if (indices.size != count) {
                println("Veuillez fournir exactement $count indice(s). Recommencez.")
                continue
            }
            // Vérifier validité des indices et unicité
            if (indices.any { it !in hand.indices }) {
                println("Un ou plusieurs indices sont hors de portée. Recommencez.")
                continue
            }
            if (indices.distinct().size != indices.size) {
                println("Indices dupliqués détectés. Recommencez.")
                continue
            }

            val selected = indices.map { hand[it] }

            return selected
        }
    }
}