package generation.general

val verticalSides = setOf(Direction.up, Direction.down)

object CoreSide {
  const val end = "end"
  const val void = "void"
}

enum class ConnectionLogic {
  connectWhenPossible,
  neutral,
  endpointWhenPossible
}

data class Side(
    val mine: Any,
    val other: Set<Any>,
    val isTraversable: Boolean = true,
    val connectionLogic: ConnectionLogic = ConnectionLogic.neutral
)

fun newSide(mine: Any, other: Set<Any> = setOf(mine), connectionLogic: ConnectionLogic = ConnectionLogic.neutral) =
    Side(mine, other, connectionLogic = connectionLogic)

val endpoint = newSide(CoreSide.end, setOf(CoreSide.void))
