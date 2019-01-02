package marloth.integration

import configuration.loadConfig
import configuration.saveConfig
import marloth.clienting.input.GameInputConfig
import rendering.DisplayConfig
import simulation.ViewMode

data class GameplayConfig(
    var defaultPlayerView: ViewMode = ViewMode.thirdPerson
)

data class GameConfig(
    var display: DisplayConfig = DisplayConfig(),
    var gameplay: GameplayConfig = GameplayConfig(),
    var input: GameInputConfig = GameInputConfig()
)

val gameConfigFile = "gameConfig.yaml"

fun saveGameConfig(config: GameConfig) {
  saveConfig(gameConfigFile, config)
}

fun loadGameConfig(): GameConfig {
  val config = loadConfig<GameConfig>(gameConfigFile)
  if (config != null)
    return config

  val newConfig = GameConfig()
  saveGameConfig(newConfig)
  return newConfig
}