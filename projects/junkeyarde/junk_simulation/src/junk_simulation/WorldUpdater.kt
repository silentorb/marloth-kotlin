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
  val actor = world.activeCreature!!

  val action = Action(
      actor = actor.id,
      ability = actor.abilities.first().id,
      target = world.player.id
  )

  return startTurn(world, action)
}

fun replaceCreature(creatures: CreatureMap, creature: Creature): CreatureMap =
    creatures.plus(Pair(creature.id, creature))


fun nextRound(world: World): World {
  val turns = prepareTurns(world.creatures.values)
  val creatures = if (world.enemies.size == 0)
    world.creatures.plus(enemiesWhenEmpty(world.wave))
  else
    world.creatures

  return world.copy(
      turns = turns.drop(1),
      animation = null,
      creatures = creatures,
      activeCreatureId = turns.first()
  )
}

fun damageCreature(world: World, target: Creature): CreatureMap {
  val life = Math.max(0, target.life - 2)
  return if (life > 0)
    replaceCreature(world.creatures, target.copy(life = life))
  else
    world.creatures.minus(target.id)
}

fun continueTurn(world: World, action: Action): World {
  val target = world.creatures[action.target]!!
  val creatures = damageCreature(world, target)

  return if (world.turns.size > 1)
    startAiTurn(world.copy(
        turns = world.turns.minus(world.activeCreatureId!!),
        animation = null,
        creatures = creatures
    ))
  else {
    nextRound(world.copy(creatures = creatures))
  }
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

fun isDead(creature: Creature): Boolean =
    creature.life != 0