package intellect

import simulation.*

fun spiritAttack(knowledge: Knowledge, pursuit: Pursuit): Commands {
  val world = knowledge.world
  val target = knowledge.visibleCharacters
      .map { knowledge.world.characterTable[it]!! }
      .first { it.id == pursuit.target }
  val character = world.characterTable[knowledge.spiritId]!!
  val body = world.bodyTable[knowledge.spiritId]!!
  val targetBody = world.bodyTable[target.id]!!
  val offset = targetBody.position - body.position
  return spiritNeedsFacing(knowledge, offset, 0.1f) {
    listOf(Command(CommandType.attack, character.id))
  }
}

//fun tryAiAttack(spirit: Spirit): NewMissile? {
//  val child = spirit.child
//  val attack = child.abilities[0]
//  if (canUse(child, attack)) {
//    val enemies = child.faction.enemies.asSequence().filter { it.isAlive }
//    val enemy = enemies.firstOrNull { it.body.position.distance(child.body.position) <= attack.definition.range }
//    if (enemy != null) {
//      val direction = (enemy.body.position - child.body.position).normalize()
//      setCharacterFacing(child, direction)
//      return characterAttack(child, attack, direction)
//    }
//  }
//
//  return null
//}

fun getVisibleCharacters(world: World, character: Character): List<Character> {
//  val enemies = world.characters.filter { it.faction != child.faction }
  return world.characters.filter { it.id != character.id && canSee(world, character, it) }
}

//fun checkEnemySighting(world: World, child: Character): Spirit? {
//  val visibleEnemy = getVisibleEnemy(world, child)
//  return if (visibleEnemy == null)
//    null
//  else
//    Spirit(
//        mode = GoalType.kill,
//        target = visibleEnemy
//    )
//}

//fun updateAttack(world: World, spirit: Spirit): SpiritUpdateResult {
//  val visibleEnemy = getVisibleEnemy(world, spirit)
//  return if (visibleEnemy == null)
//    SpiritUpdateResult(Spirit(mode = GoalType.idle))
//  else
//    SpiritUpdateResult(spirit.state)
//}
