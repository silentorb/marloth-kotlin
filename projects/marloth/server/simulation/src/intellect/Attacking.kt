package intellect

import simulation.*
import simulation.changing.setCharacterFacing

fun spiritAttack(knowledge: Knowledge, pursuit: Pursuit): Commands {
  val target = knowledge.visibleCharacters.first { it.id == pursuit.target }
  val character = knowledge.character
  val offset = target.body.position - character.body.position
  return spiritNeedsFacing(knowledge, offset, 0.1f) {
    listOf(Command(CommandType.attack, character.id))
  }
}

//fun tryAiAttack(spirit: Spirit): NewMissile? {
//  val character = spirit.character
//  val attack = character.abilities[0]
//  if (canUse(character, attack)) {
//    val enemies = character.faction.enemies.asSequence().filter { it.isAlive }
//    val enemy = enemies.firstOrNull { it.body.position.distance(character.body.position) <= attack.definition.range }
//    if (enemy != null) {
//      val direction = (enemy.body.position - character.body.position).normalize()
//      setCharacterFacing(character, direction)
//      return characterAttack(character, attack, direction)
//    }
//  }
//
//  return null
//}

fun getVisibleCharacters(world: World, character: Character): List<Character> {
//  val enemies = world.characters.filter { it.faction != character.faction }
  return world.characters.filter { it.id != character.id && canSee(character, it) }
}

//fun checkEnemySighting(world: World, character: Character): Spirit? {
//  val visibleEnemy = getVisibleEnemy(world, character)
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
