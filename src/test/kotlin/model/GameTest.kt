package model

import model.player.Player
import model.player.ai.EvaluateAi
import model.player.ai.RandomAi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameTest {
    @Test
    fun `startGame runs and assigns roles`() {
        val players: MutableList<Player> = mutableListOf(
            RandomAi("p1", mutableListOf()),
            EvaluateAi("p2", mutableListOf()),
            RandomAi("p3", mutableListOf()),
            EvaluateAi("p4", mutableListOf())
        )
        val game = Game(Game.GameParameters(nbPlayers = 4), players)

        // Should not throw
        game.startGame()

        // After startGame, roles should have been assigned (one PRESIDENT and one ASSHOLE)
        val roles = players.map { it.role }
        assertEquals(1, roles.count { it == Player.Role.PRESIDENT })
        assertEquals(1, roles.count { it == Player.Role.ASSHOLE })
    }
}