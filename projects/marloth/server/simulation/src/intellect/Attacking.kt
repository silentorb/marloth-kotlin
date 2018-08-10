package intellect

import simulation.NewMissile
import simulation.canUse
import simulation.changing.setCharacterFacing
import simulation.characterAttack

fun tryAiAttack(spirit: Spirit): NewMissile? {
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
