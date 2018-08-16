package intellect

import simulation.Character
import simulation.NewMissile
import simulation.World
import simulation.canUse
import simulation.changing.setCharacterFacing
import simulation.characterAttack

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
  return world.characters.filter { canSee(character, it) }
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
