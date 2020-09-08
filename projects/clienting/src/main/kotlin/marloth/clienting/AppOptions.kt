package marloth.clienting

import silentorb.mythic.configuration.loadYamlFile
import silentorb.mythic.configuration.saveYamlFile
import marloth.clienting.audio.AudioConfig
import marloth.clienting.input.GameInputConfig
import silentorb.mythic.lookinglass.DisplayConfig

data class GameConfig(
    val placeholder: Boolean = true
)

data class AppOptions(
    val audio: AudioConfig = AudioConfig(),
    val display: DisplayConfig = DisplayConfig(),
    val game: GameConfig = GameConfig(),
    val input: GameInputConfig = GameInputConfig()
)

const val gameConfigFile = "gameConfig.yaml"

fun saveGameConfig(options: AppOptions) {
  saveYamlFile(gameConfigFile, options)
}

fun loadGameConfig(): AppOptions {
  val config = loadYamlFile<AppOptions>(gameConfigFile)
  if (config != null)
    return config

  val newConfig = AppOptions()
  saveGameConfig(newConfig)
  return newConfig
}
