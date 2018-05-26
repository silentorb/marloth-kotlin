package intellect

import org.joml.minus
import randomly.Dice
import simulation.*
import simulation.changing.characterMove
import simulation.changing.setCharacterFacing

fun getAiCharacters(world: World) =
    world.characters.filter { isPlayer(world, it) }

fun setDestination(world: World, spirit: Spirit): SpiritState {
  val location = spirit.body.node
  val options = world.meta.nodes
      .filter { it != location }
      .filter { it.type != NodeType.space }

  val destination = Dice.global.getItem(options)
  val path = findPath(location, destination)
  assert(path != null)
  assert(path!!.any())
  return SpiritState(SpiritMode.moving, path)
}

fun updatePath(node: Node, path: List<Node>): List<Node> {
  val index = path.indexOf(node)
  return if (index == -1)
    path
  else
    path.drop(index + 1)
}

fun moveAi(world: World, spirit: Spirit): SpiritState {
  val node = spirit.body.node
  val newPath = updatePath(node, spirit.state.path!!)

  if (newPath.none())
    return SpiritState(SpiritMode.idle)

  val nextNode = newPath.first()
  val face = node.walls.firstOrNull { getOtherNode(node, it) == nextNode }
//  assert(face != null)
//
//  val direction = getFloor(face!!).middle - spirit.body.position
//  characterMove(spirit.character, direction)
  return spirit.state
}

fun updateAiState(world: World, spirit: Spirit): SpiritState {
  return when (spirit.state.mode) {
    SpiritMode.idle -> setDestination(world, spirit)
    SpiritMode.moving -> moveAi(world, spirit)
  }
}

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

fun updateAi(world: World, spirit: Spirit): NewMissile? {
  spirit.state = updateAiState(world, spirit)
  return null
//  return tryAiAttack(spirit)
}