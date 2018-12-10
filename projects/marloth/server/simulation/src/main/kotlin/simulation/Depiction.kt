package simulation

import mythic.ent.Entity
import mythic.ent.Id
import scenery.AnimationId
import scenery.ArmatureId

typealias AnimationDurationMap = Map<ArmatureId, Map<AnimationId, Float>>

enum class DepictionType {
  billboard,
  child,
  spikyBall,
  monster,
  person,
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
) : Entity

fun mapAnimation(world: World, id: AnimationId): AnimationId {
  if (id == AnimationId.stand)
    return AnimationId.girlStand

  return id
}

fun updateAnimation(world: World, animationDurations: AnimationDurationMap, animation: DepictionAnimation, strength: Float, delta: Float): DepictionAnimation {
  val animationId = mapAnimation(world, animation.animationId)
  val duration = animationDurations[ArmatureId.child]!![animationId]!!
  val nextValue = animation.animationOffset + 1f * delta
  val finalValue = if (nextValue >= duration)
    nextValue % duration
  else
    nextValue

  return animation.copy(
      animationOffset = finalValue,
      strength = strength
  )
}

fun getUpdatedAnimationDistributions(primary: AnimationId, animations: List<DepictionAnimation>, delta: Float): List<Float> {
  // Only for prerformance.  The same result would be returned without this clause.
  if (animations.size == 1)
    return listOf(1f)

  val transitionSpeed = 1f
  val offset = transitionSpeed * delta
  val initial = animations.map {
    if (it.animationId == primary)
      it.strength + offset
    else
      Math.max(0f, it.strength - offset)
  }

  val sum = initial.sum()
  return initial.map { it / sum }
}

fun updateAnimations(world: World, animationDurations: AnimationDurationMap, id: Id, animations: List<DepictionAnimation>, delta: Float): List<DepictionAnimation> {
  val body = world.bodyTable[id]!!
  val speed = body.velocity.length()
  val primaryAnimation = if (speed == 0f)
    AnimationId.stand
//  else if (speed < 1f)
//    AnimationId.walk
  else
    AnimationId.walk

//  val primaryAnimation = AnimationId.walk
  val animationsPlus = if (animations.none { it.animationId == primaryAnimation })
    animations.plus(DepictionAnimation(primaryAnimation, 0f, 0f))
  else
    animations

  val distributions = getUpdatedAnimationDistributions(primaryAnimation, animationsPlus, delta)
  val distributedAnimations = animationsPlus.zip(distributions) { animation, strength -> updateAnimation(world, animationDurations, animation, strength, delta) }
  val result = distributedAnimations.filter { it.strength > 0f }
  return result
}

fun updateDepiction(world: World, animationDurations: AnimationDurationMap): (Depiction) -> Depiction = { depiction ->
  if (depiction.animations.none())
    depiction
  else
    depiction.copy(
        animations = updateAnimations(world, animationDurations, depiction.id, depiction.animations, simulationDelta)
    )
}
//
//fun updateDepictions(world: World, animationDurations: AnimationDurationMap): List<Depiction> =
//    world.deck.depictions.map { depiction ->
//
//    }