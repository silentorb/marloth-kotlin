package generation.general

val verticalSides = setOf(Direction.up, Direction.down)

object CoreSide {
  const val end = "end"
  const val void = "void"
}

data class Side(
    val mine: Any,
    val other: Any
)

fun newSide(mine: Any, other: Any) =
    Side(mine, other)


fun newSide(mine: Any) =
    Side(mine, mine)

val endpoint = newSide(CoreSide.end, CoreSide.void)
