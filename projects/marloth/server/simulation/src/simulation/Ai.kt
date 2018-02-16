package simulation

import org.joml.minus

fun getAiPlayers(world: World) =
    world.characters.filter { isPlayer(world, it) }

fun updateEnemy(character: Character): NewMissile? {
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