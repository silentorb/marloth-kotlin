package junk_simulation

data class GameCommand(
    val type: CommandType,
    val data: Any? = null
)

data class Action(
    val ability: Id,
    val creature: Id?
)

fun takeTurn(world: World, action: Action): World {
  val actor = world.turns.first()

  return world.copy(
      turns = world.turns.drop(1),
      animation = Animation(
          type = AnimationType.missile,
          actor = actor,
          target = action.creature!!,
          ability = action.ability,
          progress = 0f
      )
  )
}

fun updateAnimation(animation: Animation, delta: Float): Animation? {
  val missileSpeed = 2f
  val progress = animation.progress + missileSpeed * delta
  return if (progress < 1f)
    animation.copy(
        progress = progress
    )
  else
    null
}
