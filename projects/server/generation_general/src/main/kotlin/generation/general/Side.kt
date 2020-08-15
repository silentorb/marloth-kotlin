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

data class Side(
    val mine: Any,
    val other: Set<Any> = setOf(mine),
    val isTraversable: Boolean = true,
    val connectionLogic: ConnectionLogic = ConnectionLogic.optional,
    val isUniversal: Boolean = false
)

val endpoint = Side(CoreSide.end, setOf(CoreSide.void), isUniversal = true, isTraversable = false)
