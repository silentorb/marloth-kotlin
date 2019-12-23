package simulation.misc

enum class CellAttribute {
  categoryCommon,
  categoryDiagonal,
  lockedRotation,
  exit,
  home,
  fullFloor, // The majority of the floor is contiguous and available for placing objects
  traversable,
  tunnel
}
