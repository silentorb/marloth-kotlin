package marloth.game.integration

import simulation.misc.GameModeConfig
import simulation.misc.LivesMode

fun sandboxGameMode() = GameModeConfig(
    lives = LivesMode.infinite
)

fun newGameModeConfig(): GameModeConfig =
    sandboxGameMode()
