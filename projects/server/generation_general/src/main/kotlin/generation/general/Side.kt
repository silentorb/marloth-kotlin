package generation.general

val verticalSides = setOf(Direction.up, Direction.down)

// The standard 4 heights are incremental quarters + 10 for floor height padding
object StandardHeights {
  val first = 10
  val second = 35
  val third = 60
  val fourth = 85
}

data class Side(
    val mine: String,
    val other: Set<String>,
    val height: Int = StandardHeights.first,
    val isTraversable: Boolean = true
)
