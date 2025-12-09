package model.player.ai

import model.Card
import model.player.Player
import model.player.PlayerUtils

abstract class Ai(
    hand: MutableList<Card>,
    id: String,
    playerUtils: PlayerUtils = PlayerUtils,
    val aiUtils: AiUtils = AiUtils
) : AiInterface, Player(hand, id)