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
  val entities: MutableMap<Id, Entity> = mutableMapOf()
  val players: MutableList<Player> = mutableListOf()
  val bodies: MutableMap<Int, Body> = mutableMapOf()
  val characters: MutableMap<Int, Character> = mutableMapOf()
  val missiles: MutableMap<Int, Missile> = mutableMapOf()
  fun getAndSetNextId() = _nextId++

  fun createEntity(type: EntityType): Entity {
    val id = getAndSetNextId()
    val entity = Entity(id, type)
    entities[id] = entity
    return entity
  }

  fun createBody(type: EntityType, shape: collision.Shape, position: Vector3): Body {
    val entity = createEntity(type)
    val body = Body(entity.id, shape, position, Quaternion(), Vector3())
    bodies[entity.id] = body
    return body
  }

  fun createCharacter(position: Vector3, health: Int): Character {
    val body = createBody(EntityType.character, commonShapes[EntityType.character]!!, position)
    val character = Character(body.id, body, health)
    characters[body.id] = character
    return character
  }

  fun createMissile(owner: Character, position: Vector3, velocity: Vector3): Missile {
    val body = createBody(EntityType.missile, commonShapes[EntityType.missile]!!, position)
    body.velocity = velocity
    val missile = Missile(body.id, body, owner)
    missiles[body.id] = missile
    return missile
  }

  fun createPlayer(id: Int): Player {
    val position = meta.nodes.first().position + Vector3(0f, 0f, 1f)
    val character = createCharacter(position, 100)
    val player = Player(character, id)
    players.add(player)
    character.abilities.add(Ability(0.2f))
    return player
  }
}

class WorldUpdater(val world: World) {

  fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float): NewMissile? {
    if (commands.isEmpty())
      return null

    playerMove(world, player.character.body, commands, delta)
    return playerShoot(world, player.character, commands)
  }

  fun applyCommands(players: Players, commands: Commands<CommandType>, delta: Float): List<NewMissile> {
    val result = players.mapNotNull { player ->
      applyPlayerCommands(player, commands.filter({ it.target == player.playerId }), delta)
    }

    val remainingCommands = commands.filter({ it.target == 0 || it.target > maxPlayerCount })
    for (command in remainingCommands) {
      if (command.type == CommandType.joinGame) {
        world.createPlayer(world.players.size + 1)
      }
    }

    return result
  }

  fun updateCharacter(character: Character, delta: Float) {
    character.abilities.forEach { updateAbility(it, delta) }
  }

  fun updateCharacters(delta: Float) {
    world.characters.values.forEach { updateCharacter(it, delta) }
  }

  fun createMissiles(newMissiles: List<NewMissile>) {
    for (newMissile in newMissiles) {
      world.createMissile(newMissile.owner, newMissile.position, newMissile.direction)
    }
  }

  fun getFinished(): List<Int> {
    return world.missiles.values
        .filter { isFinished(world, it) }
        .map { it.id }
        .plus(world.characters.values
            .filter { isFinished(world, it) }
            .map { it.id })
  }

  fun removeFinished(finished: List<Int>) {
    world.missiles.minusAssign(finished)
    world.bodies.minusAssign(finished)
    world.entities.minusAssign(finished)
    world.characters.minusAssign(finished)
  }

  fun update(commands: Commands<CommandType>, delta: Float) {
    updateCharacters(delta)
    world.missiles.values.forEach { updateMissile(world, it, delta) }
    val newMissiles = applyCommands(world.players, commands, delta)

    val finished = getFinished()
    removeFinished(finished)

    createMissiles(newMissiles)
  }
}
