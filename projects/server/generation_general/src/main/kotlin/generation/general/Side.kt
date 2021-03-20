package generation.general

val verticalSides = setOf(Direction.up, Direction.down)

object CoreSide {
  const val end = "end"
  const val void = "void"
}

enum class ConnectionLogic {
  minimal,
  required,
  optional,
}

// The standard 4 heights are incremental quarters + 10 for floor height padding
object StandardHeights {
  val first = 10
  val second = 35
  val third = 60
  val fourth = 85
}

// Only used until the block side refactoring is finished
val tempConnectionContract = "open"

data class Side(
    val mine: String = tempConnectionContract,
    val other: Set<String> = setOf(tempConnectionContract),
    val height: Int = StandardHeights.first,
    val isTraversable: Boolean = true
)

val endpoint = Side(isTraversable = false)
