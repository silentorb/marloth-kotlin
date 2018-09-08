package simulation

enum class DepictionType {
  billboard,
  character,
  missile,
  monster,
  none,
  test,
  wallLamp,
  world
}

data class Depiction(
    override val id: Id,
    val type: DepictionType
) : EntityLike