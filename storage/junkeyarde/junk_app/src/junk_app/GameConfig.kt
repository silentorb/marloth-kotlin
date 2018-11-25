package junk_app

import configuration.loadConfig
import configuration.saveConfig
import rendering.DisplayConfig

data class GameConfig(
    var display: DisplayConfig = DisplayConfig()
)

val gameConfigFile = "junkGameConfig.yaml"

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
