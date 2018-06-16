package junk_simulation

data class GameCommand(
    val type: CommandType,
    val data: Any? = null
)

fun startTurn(world: World, action: Action): World {
  return world.copy(
      animation = Animation(
          type = AnimationType.missile,
          action = action,
          progress = 0f,
          delay = if (world.player.id == action.actor) 0.1f else 0.3f
      )
  )
}

fun startAiTurn(world: World): World {
  val actor = world.activeCreature

  val action = Action(
      actor = actor.id,
      ability = actor.abilities.first().id,
      target = world.player.id
  )

  return startTurn(world, action)
}

fun replaceCreature(creatures: CreatureMap, creature: Creature): CreatureMap =
    creatures.plus(Pair(creature.id, creature))

fun continueTurn(world: World, action: Action): World {
  val target = world.creatures[action.target]!!
  val creatures = replaceCreature(world.creatures, target.copy(life = Math.max(0, target.life - 1)))
  return if (world.turns.size > 1)
    startAiTurn(world.copy(
        turns = world.turns.drop(1),
        animation = null,
        creatures = creatures
    ))
  else
    world.copy(
        turns = newTurn(creatures.values),
        animation = null,
        creatures = creatures
    )
}

fun updateAnimation(animation: Animation, delta: Float): Animation? {
  if (animation.delay > 0f)
    return animation.copy(delay = Math.max(0f, animation.delay - delta))

  val missileSpeed = 1.5f
  val progress = animation.progress + missileSpeed * delta
  return if (progress < 1f)
    animation.copy(
        progress = progress
    )
  else
    null
}
