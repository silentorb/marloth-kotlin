package generation.general

val verticalSides = setOf(Direction.up, Direction.down)

// The standard 4 heights are incremental quarters + 10 for floor height padding
object StandardHeights {
  val first = 100
  val firstB = 225
  val second = 350
  val third = 600
  val fourth = 850
}

data class Side(
    val mine: String,
    val other: Set<String>,
    val height: Int = StandardHeights.first,
    val isTraversable: Boolean = true,
    val isEssential: Boolean = false,
    val canMatchEssential: Boolean = true,
    val greedy: Boolean = false,
)
