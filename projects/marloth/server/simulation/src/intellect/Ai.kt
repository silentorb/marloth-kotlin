package intellect

import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.minus
import org.joml.plus
import physics.Force
import randomly.Dice
import simulation.*
import simulation.changing.setCharacterFacing

enum class SpiritActionType {
  move
}

data class SpiritAction(
    val type: SpiritActionType,
    val offset: Vector3
)

data class SpiritUpdateResult(
    val state: SpiritState,
    val actions: List<SpiritAction> = listOf()
)

fun getAiCharacters(world: World) =
    world.characters.filter { isPlayer(world, it) }

fun setDestination(world: World, spirit: Spirit): SpiritState {
  val location = spirit.body.node
  val options = world.meta.nodes
      .filter { it != location }
      .filter { it.type != NodeType.space }

  val destination = Dice.global.getItem(options)
//  val destination = options[(location.index + 6) % options.size]
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

fun moveSpirit(spirit: Spirit): SpiritUpdateResult {
  val node = spirit.body.node
  val newPath = updatePath(node, spirit.state.path!!)

  if (newPath.none())
    return SpiritUpdateResult(SpiritState(SpiritMode.idle))

  val nextNode = newPath.first()
  val face = node.walls.firstOrNull { getOtherNode(node, it) == nextNode }

  if (face == null) {
//    throw Error("Not supported")
//    println("Not supported!!!")
    return SpiritUpdateResult(spirit.state)
  } else {
    val edge = getFloor(face)
    val position = spirit.body.position
    val nearestPoint = edge.vertices.sortedBy { it.distance(position) }.first()
    val target = (edge.middle + nearestPoint) / 2f
//    val target = edge.middle
    val direction = (target - position).normalize()
//    characterMove(spirit.character, direction)
    return SpiritUpdateResult(spirit.state, listOf(SpiritAction(SpiritActionType.move, direction)))
  }
}

fun updateAiState(world: World, spirit: Spirit): SpiritUpdateResult {
  return when (spirit.state.mode) {
    SpiritMode.idle -> SpiritUpdateResult(setDestination(world, spirit))
    SpiritMode.moving -> moveSpirit(spirit)
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

fun handleSpiritAction(spirit: Spirit, action: SpiritAction): Force? {
  when (action.type) {
    SpiritActionType.move -> {
      setCharacterFacing(spirit.character, action.offset)
      return Force(spirit.body, action.offset, 6f)
    }
  }

  return null
}

data class CharacterResult(
    val forces: List<Force> = listOf(),
    val newMissile: NewMissile? = null
)

fun updateSpirit(world: World, spirit: Spirit): CharacterResult {
  val result = updateAiState(world, spirit)
  spirit.state = result.state
  val forces = result.actions.mapNotNull { handleSpiritAction(spirit, it) }
  return CharacterResult(forces = forces)
//  return tryAiAttack(spirit)
}