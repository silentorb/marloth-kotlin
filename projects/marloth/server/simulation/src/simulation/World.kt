package simulation

import commanding.CommandType
import haft.Commands
import mythic.spatial.*
import org.joml.plus

val maxPlayerCount = 4

typealias Players = List<Player>

data class World(
    val meta: AbstractWorld
) {
  private var _nextId = 1
  val players: MutableList<Player> = mutableListOf()
  val bodies: MutableMap<Int, Body> = mutableMapOf()
  val characters: MutableMap<Int, Character> = mutableMapOf()
  fun getAndSetNextId() = _nextId++
}

class WorldUpdater(val world: World) {

  fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float) {
    if (commands.isEmpty())
      return

    playerMove(world, world.bodies[player.id]!!, commands, delta)
//    playerShoot(world, player, commands)
  }

  fun applyCommands(players: Players, commands: Commands<CommandType>, delta: Float) {
    for (player in players) {
      applyPlayerCommands(player, commands.filter({ it.target == player.playerId }), delta)
    }

    val remainingCommands = commands.filter({ it.target == 0 || it.target > maxPlayerCount })
    for (command in remainingCommands) {
      if (command.type == CommandType.joinGame) {
        createPlayer(world, world.players.size + 1)
      }
    }
  }

  fun update(commands: Commands<CommandType>, delta: Float) {
    applyCommands(world.players, commands, delta)
  }
}

fun createBody(world: World, position: Vector3): Body {
  val id = world.getAndSetNextId()
  val body = Body(id, position)
  world.bodies[id] = body
  return body
}

fun createCharacter(world: World, position: Vector3): Character {
  val body = createBody(world, position)
  val character = Character(body.id)
  world.characters[body.id] = character
  return character
}

fun createPlayer(world: World, id: Int): Player {
  val position = world.meta.nodes.first().position + Vector3(0f, 0f, 1f)
  val character = createCharacter(world, position)
  val player = Player(character.id, id)
  character.abilities.add(Ability(1f))
  return player
}