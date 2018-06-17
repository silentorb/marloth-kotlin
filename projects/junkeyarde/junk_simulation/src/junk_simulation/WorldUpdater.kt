package junk_simulation

data class GameCommand(
    val type: CommandType,
    val data: Any? = null
)

fun startTurn(world: World, action: Action): World {
  return world.copy(
      animation = Animation(
          action = action,
          progress = 0f,
          delay = if (world.player.id == action.actor) 0.1f else 0.3f
      )
  )
}

fun startAiTurn(world: World): World {
  val actor = world.activeCreature!!

  val action = Action(
      type = ActionType.attack,
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

data class ActionResult(
    val world: World,
    val animation: Animation?
)

fun damageCreature(world: World, target: Creature): ActionResult {
  val life = Math.max(0, target.life - 2)
  val creatures = replaceCreature(world.creatures, target.copy(life = life))
  val animation = if (life == 0)
    Animation(
        action = Action(
            type = ActionType.death,
            actor = target.id,
            target = target.id
        ),
        progress = 0f,
        delay = 0.1f
    )
  else
    null

  return ActionResult(world.copy(
      creatures = creatures
  ), animation)
}

fun creatureDied(world: World, target: Creature): ActionResult {
  return ActionResult(world.copy(
      creatures = world.creatures.minus(target.id),
      turns = world.turns.minus(target.id)
  ),
      null)
}

fun continueTurn(oldWorld: World, action: Action): World {
  val target = oldWorld.creatures[action.target]!!
  val (world, animation) = when (action.type) {
    ActionType.attack -> damageCreature(oldWorld, target)
    ActionType.death -> creatureDied(oldWorld, target)
    else -> throw Error("Not implemented.")
  }

  if (animation != null)
    return world.copy(animation = animation)

  return if (world.turns.any())
    startAiTurn(world.copy(
        turns = world.turns.drop(1),
        activeCreatureId = world.turns.first(),
        animation = null
    ))
  else {
    nextRound(world)
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

fun isAlive(creature: Creature): Boolean =
    creature.life != 0