package intellect

import org.joml.minus
import simulation.*
import simulation.changing.setCharacterFacing

fun getAiCharacters(world: World) =
    world.characters.filter { isPlayer(world, it) }

fun tryAiMove(world: World, spirit: Spirit) {
  val character = spirit.character

}

fun tryAiAttack(spirit: Spirit): NewMissile? {
  return null
  val character = spirit.character
  val attack = character.abilities[0]
  if (canUse(character, attack)) {
    val enemies = character.faction.enemies.asSequence().filter { it.isAlive }
    val enemy = enemies.firstOrNull { it.body.position.distance(character.body.position) <= attack.definition.range }
    if (enemy != null) {
      val direction = (enemy.body.position - character.body.position).normalize()
      setCharacterFacing(character, direction)
      return characterAttack(character, attack, direction)
    }
  }

  return null
}

fun updateAi(world: World, spirit: Spirit): NewMissile? {
  tryAiMove(world, spirit)
  return tryAiAttack(spirit)
}