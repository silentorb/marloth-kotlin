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
    val mineOld: Any = "Nothing",
    val otherOld: Set<Any> = setOf(mineOld),
    val mine: String = tempConnectionContract,
    val other: String = tempConnectionContract,
    val height: Int = StandardHeights.first,
    val isTraversable: Boolean = true,
    val connectionLogic: ConnectionLogic = ConnectionLogic.optional,
    val isUniversal: Boolean = false
)

val endpoint = Side(CoreSide.end, setOf(CoreSide.void), isUniversal = true, isTraversable = false)
