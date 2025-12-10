package model.player.ai

import model.Card
import model.player.Player
import model.player.PlayerUtils

abstract class Ai(
    id: String,
    hand: MutableList<Card>
) : Player(id, hand), AiInterface