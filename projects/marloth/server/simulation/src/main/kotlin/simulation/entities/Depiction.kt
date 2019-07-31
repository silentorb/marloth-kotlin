package simulation.entities

import mythic.ent.Id
import scenery.*
import simulation.main.Deck
import simulation.main.World
import simulation.main.simulationDelta

typealias AnimationDurationMap = Map<ArmatureId, Map<AnimationId, Float>>

enum class DepictionType {
  billboard,
  child,
  spikyBall,
  monster,
  person,
  none,
  staticMesh,
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
    val type: DepictionType,
    val mesh: MeshName? = null,
    val texture: TextureName? = null,
    val animations: List<DepictionAnimation> = listOf()
)

fun mapAnimation(deck: Deck, id: AnimationId): AnimationId {
//  if (id == AnimationId.stand)
//    return AnimationId.stand

  return id
}

fun updateAnimation(deck: Deck, animationDurations: AnimationDurationMap, animation: DepictionAnimation, strength: Float, delta: Float): DepictionAnimation {
  val animationId = mapAnimation(deck, animation.animationId)
  val duration = animationDurations[ArmatureId.person]!![animationId]
  val nextValue = animation.animationOffset + 1f * delta
  val finalValue = if (duration != null)
    if (duration == 0f)
      0f
    else if (nextValue >= duration)
      nextValue % duration
    else
      nextValue
  else
    nextValue

  assert(!finalValue.isNaN())
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

fun updateAnimations(deck: Deck, animationDurations: AnimationDurationMap, id: Id, animations: List<DepictionAnimation>, delta: Float): List<DepictionAnimation> {
  val body = deck.bodies[id]!!
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
  val distributedAnimations = animationsPlus.zip(distributions) { animation, strength -> updateAnimation(deck, animationDurations, animation, strength, delta) }
  val result = distributedAnimations.filter { it.strength > 0f }
  return result
}

fun updateDepiction(deck: Deck, animationDurations: AnimationDurationMap): (Id, Depiction) -> Depiction = { id, depiction ->
  if (depiction.animations.none())
    depiction
  else
    depiction.copy(
        animations = updateAnimations(deck, animationDurations, id, depiction.animations, simulationDelta)
    )
}
