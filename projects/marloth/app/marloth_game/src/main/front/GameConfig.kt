package front

import configuration.loadConfig
import configuration.saveConfig
import mythic.platforming.DisplayConfig
import simulation.ViewMode

data class GameplayConfig(
    var defaultPlayerView: ViewMode = ViewMode.topDown
)

data class GameConfig(
    var display: DisplayConfig = DisplayConfig(),
    var gameplay: GameplayConfig = GameplayConfig()
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