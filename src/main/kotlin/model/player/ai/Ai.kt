package model.player.ai

import model.Card
import model.player.Player

abstract class Ai(
    id: String,
    hand: MutableList<Card>
) : Player(id, hand), AiInterface