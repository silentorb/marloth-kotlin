package simulation.misc

enum class CellAttribute {
  categoryCommon,
  categoryDiagonal,
  lockedRotation,
  exit,
  home,
  traversable,

  // Only used in development and not in game logic:
  spiralStaircase,
}
