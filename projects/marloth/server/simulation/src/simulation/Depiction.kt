package simulation

import scenery.AnimationId
import scenery.ArmatureId

typealias AnimationDurationMap = Map<ArmatureId, Map<AnimationId, Float>>

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
    val animationId: AnimationId = AnimationId.none,
    val animationOffset: Float = -1f,
    val strength: Float = 1f
)

data class Depiction(
    override val id: Id,
    val type: DepictionType,
    val animations: List<DepictionAnimation> = listOf()
) : EntityLike

fun mapAnimation(world: World, id: AnimationId): AnimationId {
  if(id == AnimationId.stand)
    return AnimationId.girlStand

  return id
}

fun updateAnimation(world: World, animationDurations: AnimationDurationMap, animation: DepictionAnimation): DepictionAnimation {
  val animationId = mapAnimation(world, animation.animationId)
  val duration = animationDurations[ArmatureId.child]!![animationId]!!
  val nextValue = animation.animationOffset + 1f * simulationDelta
  val finalValue = if (nextValue >= duration)
    nextValue % duration
  else
    nextValue

  return animation.copy(
      animationOffset = finalValue
  )
}

fun updateDepictions(world: World, animationDurations: AnimationDurationMap): List<Depiction> =
    world.deck.depictions.map { depiction ->
      if (depiction.animations.none())
        depiction
      else
        depiction.copy(
            animations = depiction.animations.map { updateAnimation(world, animationDurations, it) }
        )
    }