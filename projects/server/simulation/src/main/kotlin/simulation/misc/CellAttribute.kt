package simulation.misc

enum class CellAttribute {
  categoryCommon,
  categoryDiagonal,
  lockedRotation,
  exit,
  home,
  traversable,
  unique,

  // Only used in development and not in game logic:
  spiralStaircase,
}
