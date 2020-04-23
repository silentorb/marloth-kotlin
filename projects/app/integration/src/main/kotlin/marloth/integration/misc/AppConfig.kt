package marloth.integration.misc

import silentorb.mythic.configuration.loadYamlFile
import silentorb.mythic.configuration.saveYamlFile
import marloth.clienting.audio.AudioConfig
import marloth.clienting.input.GameInputConfig
import silentorb.mythic.lookinglass.DisplayConfig

data class GameConfig(
    val placeholder: Boolean = true
)

data class AppConfig(
    var audio: AudioConfig = AudioConfig(),
    var display: DisplayConfig = DisplayConfig(),
    var game: GameConfig = GameConfig(),
    var input: GameInputConfig = GameInputConfig()
)

val gameConfigFile = "../gameConfig.yaml"

fun saveGameConfig(config: AppConfig) {
  saveYamlFile(gameConfigFile, config)
}

fun loadGameConfig(): AppConfig {
  val config = loadYamlFile<AppConfig>(gameConfigFile)
  if (config != null)
    return config

  val newConfig = AppConfig()
  saveGameConfig(newConfig)
  return newConfig
}
