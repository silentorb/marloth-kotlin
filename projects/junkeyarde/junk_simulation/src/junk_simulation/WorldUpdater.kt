package junk_simulation

data class GameCommand(
    val type: CommandType,
    val data: Any? = null
)

fun startTurn(world: World, action: Action): World {
  return world.copy(
      turns = world.turns.drop(1),
      animation = Animation(
          type = AnimationType.missile,
          action = action,
          progress = 0f
      )
  )
}

fun replaceCreature(creatures: CreatureMap, creature: Creature): CreatureMap =
    creatures.plus(Pair(creature.id, creature))

fun continueTurn(world: World, action: Action): World {
  val target = world.creatures[action.target]!!
  val creatures = replaceCreature(world.creatures, target.copy(life = Math.max(0, target.life - 1)))
  return world.copy(
      animation = null,
      creatures = creatures
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
