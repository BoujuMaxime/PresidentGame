package view

import model.Game
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GameConfigTest {

    @Test
    fun `GameConfig stores player count`() {
        val config = GameConfig(
            playerCount = 5,
            difficulty = Game.GameParameters.DifficultyLevel.MEDIUM,
            withCarreMagique = true,
            withTaGueule = false
        )
        assertEquals(5, config.playerCount)
    }

    @Test
    fun `GameConfig stores difficulty level`() {
        val config = GameConfig(
            playerCount = 4,
            difficulty = Game.GameParameters.DifficultyLevel.HARD,
            withCarreMagique = true,
            withTaGueule = true
        )
        assertEquals(Game.GameParameters.DifficultyLevel.HARD, config.difficulty)
    }

    @Test
    fun `GameConfig stores rule flags`() {
        val config = GameConfig(
            playerCount = 4,
            difficulty = Game.GameParameters.DifficultyLevel.EASY,
            withCarreMagique = false,
            withTaGueule = true
        )
        assertFalse(config.withCarreMagique)
        assertTrue(config.withTaGueule)
    }

    @Test
    fun `GameConfig equality works correctly`() {
        val config1 = GameConfig(4, Game.GameParameters.DifficultyLevel.MEDIUM, true, true)
        val config2 = GameConfig(4, Game.GameParameters.DifficultyLevel.MEDIUM, true, true)
        val config3 = GameConfig(5, Game.GameParameters.DifficultyLevel.MEDIUM, true, true)

        assertEquals(config1, config2)
        assertNotEquals(config1, config3)
    }

    @Test
    fun `GameConfig copy works correctly`() {
        val original = GameConfig(4, Game.GameParameters.DifficultyLevel.MEDIUM, true, true)
        val modified = original.copy(playerCount = 6)

        assertEquals(4, original.playerCount)
        assertEquals(6, modified.playerCount)
        assertEquals(original.difficulty, modified.difficulty)
    }
}
