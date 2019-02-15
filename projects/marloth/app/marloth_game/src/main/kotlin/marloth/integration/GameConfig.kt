package marloth.integration

import configuration.loadYamlFile
import configuration.saveYamlFile
import marloth.clienting.input.GameInputConfig
import rendering.DisplayConfig
import simulation.ViewMode

data class GameplayConfig(
    var defaultPlayerView: ViewMode = ViewMode.thirdPerson
)

data class AudioConfig(
    var soundVolume: Float = 0.75f
)

data class GameConfig(
    var audio: AudioConfig = AudioConfig(),
    var display: DisplayConfig = DisplayConfig(),
    var gameplay: GameplayConfig = GameplayConfig(),
    var input: GameInputConfig = GameInputConfig()
)

val gameConfigFile = "gameConfig.yaml"

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