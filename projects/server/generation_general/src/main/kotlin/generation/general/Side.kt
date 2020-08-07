package generation.general

val verticalSides = setOf(Direction.up, Direction.down)

object CoreSide {
  const val end = "end"
  const val void = "void"
}

enum class ConnectionLogic {
  required,
  neutral,
}

data class Side(
    val mine: Any,
    val other: Set<Any> = setOf(mine),
    val isTraversable: Boolean = true,
    val connectionLogic: ConnectionLogic = ConnectionLogic.neutral
)

val endpoint = Side(CoreSide.end, setOf(CoreSide.void))
