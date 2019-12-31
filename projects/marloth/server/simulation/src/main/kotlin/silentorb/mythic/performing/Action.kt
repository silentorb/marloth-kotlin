package silentorb.mythic.performing

import silentorb.mythic.breeze.AnimationName

data class ActionDefinition(
    val cooldown: Float,
    val range: Float,
    val animation: AnimationName? = null
)

data class Action(
    val cooldown: Float = 0f
)

fun updateCooldown(action: Action, isActivated: Boolean, cooldown: Float, delta: Float): Float {
  return if (isActivated)
    cooldown
  else if (action.cooldown > 0f)
    Math.max(0f, action.cooldown - delta)
  else
    0f
}
