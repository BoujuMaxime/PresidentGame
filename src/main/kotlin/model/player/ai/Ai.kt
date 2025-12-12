package model.player.ai

import model.Card
import model.player.Player

/**
 * Classe abstraite représentant une intelligence artificielle (IA) dans le jeu.
 *
 * @param id L'identifiant unique de l'IA.
 * @param hand La main initiale de l'IA, une liste mutable de cartes.
 * @param random Un paramètre optionnel pour la randomisation des décisions de l'IA (par défaut à 10).
 */
abstract class Ai(
    id: String,
    hand: MutableList<Card>,
    random: Int = 10
) : Player(id, hand), AiInterface