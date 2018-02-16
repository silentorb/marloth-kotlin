package intellect

import org.joml.minus
import simulation.*

fun getAiCharacters(world: World) =
    world.characters.filter { isPlayer(world, it) }

fun tryAiMove(world: World, spirit: Spirit) {
  val character = spirit.character

}

fun tryAiAttack(spirit: Spirit): NewMissile? {
  val character = spirit.character
  val attack = character.abilities[0]
  if (canUse(character, attack)) {
    val enemies = character.faction.enemies
    val enemy = enemies.firstOrNull { it.body.position.distance(character.body.position) <= attack.definition.range }
    if (enemy != null) {
      val direction = (enemy.body.position - character.body.position).normalize()
      return characterAttack(character, attack, direction)
    }
  }

  return null
}

fun updateAi(world: World, spirit: Spirit): NewMissile? {
  tryAiMove(world, spirit)
  return tryAiAttack(spirit)
}