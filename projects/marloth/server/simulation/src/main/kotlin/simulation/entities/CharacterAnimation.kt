package simulation.entities

import mythic.ent.Id
import scenery.AnimationId
import scenery.ArmatureId
import simulation.main.Deck

typealias AnimationName = String

data class SingleAnimation(
    val animationId: AnimationName,
    val animationOffset: Float = -1f,
    val strength: Float = 1f
)

data class CharacterAnimation(
    val animations: List<SingleAnimation>
)

fun updateAnimationOffset(animationId: AnimationName, animationDurations: AnimationDurationMap,
                          offset: Float, delta: Float): Float {
  val duration = animationDurations[ArmatureId.person]!![animationId]
  val nextValue = offset + 1f * delta
  return if (duration != null)
    if (duration == 0f)
      0f
    else if (nextValue >= duration)
      nextValue % duration
    else
      nextValue
  else
    nextValue
}

fun updateSingleAnimation(animationDurations: AnimationDurationMap,
                          delta: Float): (Float, SingleAnimation) -> SingleAnimation = { strength, animation ->
  val finalValue = updateAnimationOffset(animation.animationId, animationDurations, animation.animationOffset, delta)

  assert(!finalValue.isNaN())
  animation.copy(
      animationOffset = finalValue,
      strength = strength
  )
}

fun getUpdatedAnimationDistributions(primary: AnimationName,
                                     animations: List<SingleAnimation>, delta: Float): List<Float> {
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

fun updatePrimaryAnimation(deck: Deck, character: Id): AnimationName {
  val performance = deck.performances.values.firstOrNull { it.target == character }
  return if (performance != null) {
    performance.animation
  } else {
    val body = deck.bodies[character]!!
    val speed = body.velocity.length()
    return if (speed < 0.1f)
      AnimationId.stand.name
    else
      AnimationId.walk.name
  }
}

fun updateTargetAnimations(deck: Deck, animationDurations: AnimationDurationMap,
                           delta: Float, character: Id): (List<SingleAnimation>) -> List<SingleAnimation> = { animations ->
  val primaryAnimation = updatePrimaryAnimation(deck, character)
  val animationsPlus = if (animations.none { it.animationId == primaryAnimation })
    animations.plus(SingleAnimation(primaryAnimation, 0f, 0f))
  else
    animations
  val distributions = getUpdatedAnimationDistributions(primaryAnimation, animationsPlus, delta)
  val distributedAnimations = distributions.zip(animationsPlus, updateSingleAnimation(animationDurations, delta))
  val result = distributedAnimations.filter { it.strength > 0f }
  result
}

fun updateCharacterAnimation(deck: Deck, animationDurations: AnimationDurationMap,
                             delta: Float): (Id, CharacterAnimation) -> CharacterAnimation = { id, animation ->
  animation.copy(
      animations = updateTargetAnimations(deck, animationDurations, delta, id)(animation.animations)
  )
}
