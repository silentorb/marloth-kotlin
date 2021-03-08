package simulation.misc

enum class LivesMode {
  infinite,
  single
}

data class GameModeConfig(
    val lives: LivesMode
)
