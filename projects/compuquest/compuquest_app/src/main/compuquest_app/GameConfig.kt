package compuquest_app

import configuration.loadYamlFile
import configuration.saveYamlFile
import rendering.DisplayConfig

data class GameConfig(
    var display: DisplayConfig = DisplayConfig()
)

val gameConfigFile = "compuquestGameConfig.yaml"

fun saveGameConfig(config: GameConfig) {
  saveYamlFile(gameConfigFile, config)
}

fun loadGameConfig(): GameConfig {
  val config = loadYamlFile<GameConfig>(gameConfigFile)
  if (config != null)
    return config

  val newConfig = GameConfig()
  saveGameConfig(newConfig)
  return newConfig
}
