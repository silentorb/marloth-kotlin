package compuquest_app

import silentorb.mythic.configuration.loadYamlFile
import silentorb.mythic.configuration.saveYamlFile
import silentorb.mythic.lookinglass.DisplayConfig

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
