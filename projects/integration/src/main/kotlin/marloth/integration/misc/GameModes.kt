package marloth.integration.misc

import simulation.misc.GameModeConfig
import simulation.misc.LivesMode

fun sandboxGameMode() = GameModeConfig(
    lives = LivesMode.infinite
)

fun newGameModeConfig(): GameModeConfig =
    sandboxGameMode()
