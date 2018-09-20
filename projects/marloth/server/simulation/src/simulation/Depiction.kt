package simulation

typealias AnimationDurationMap = Map<DepictionType, List<Float>>

enum class DepictionType {
  billboard,
  child,
  missile,
  monster,
  none,
  test,
  wallLamp,
  world
}

data class DepictionAnimation(
    val animation: Int = -1,
    val animationOffset: Int = -1,
    val strength: Float = 1f
)

data class Depiction(
    override val id: Id,
    val type: DepictionType,
    val animations: List<DepictionAnimation> = listOf()
) : EntityLike

fun updateAnimation(animationDurations: AnimationDurationMap, animation: DepictionAnimation): DepictionAnimation {
  val duration = animationDurations[]
}

fun updateDepictions(animationDurations: AnimationDurationMap, world: World): List<Depiction> =
    world.deck.depictions.map { depiction ->
      if (depiction.animations.none())
        depiction
      else
        depiction.copy(
            animations = depiction.animations.map { updateAnimation(animationDurations, it) }
        )
    }