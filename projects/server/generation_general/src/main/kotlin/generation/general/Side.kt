package generation.general

val verticalSides = setOf(Direction.up, Direction.down)

object CoreSide {
  const val end = "end"
  const val void = "void"
}

data class Side(
    val mine: Any,
    val other: Set<Any>,
    val closeIfPossible: Boolean = false
)

fun newSide(mine: Any, other: Set<Any>, closeIfPossible: Boolean = false) =
    Side(mine, other, closeIfPossible)


fun newSide(mine: Any) =
    Side(mine, setOf(mine))

val endpoint = newSide(CoreSide.end, setOf(CoreSide.void))
