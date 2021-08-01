package marloth.clienting

import silentorb.mythic.configuration.loadYamlFile
import silentorb.mythic.configuration.saveYamlFile
import marloth.clienting.audio.AudioConfig
import marloth.clienting.input.InputOptions
import silentorb.mythic.lookinglass.DisplayOptions

data class GameConfig(
    val placeholder: Boolean = true
)

data class UiOptions(
    val showHud: Boolean = true,
)

data class AppOptions(
    val audio: AudioConfig = AudioConfig(),
    val display: DisplayOptions = DisplayOptions(),
    val game: GameConfig = GameConfig(),
    val input: InputOptions = InputOptions(),
    val ui: UiOptions = UiOptions(),
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
