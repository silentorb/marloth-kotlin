package simulation

import commanding.CommandType
import haft.Commands
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.plus

val maxPlayerCount = 4

typealias Players = List<Player>

data class World(
    val meta: AbstractWorld
) {
  private var _nextId = 1
  val entities: MutableMap<Id, Entity> = mutableMapOf()
  val players: MutableList<Player> = mutableListOf()
  val bodyTable: MutableMap<Int, Body> = mutableMapOf()
  val characterTable: MutableMap<Int, Character> = mutableMapOf()
  val missileTable: MutableMap<Int, Missile> = mutableMapOf()
  val factions = mutableListOf(
      Faction(this, "Misfits"),
      Faction(this, "Monsters")
  )

  val characters: MutableCollection<Character>
    get() = characterTable.values

  val bodies: MutableCollection<Body>
    get() = bodyTable.values

  val missiles: MutableCollection<Missile>
    get() = missileTable.values


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
    bodyTable[entity.id] = body
    return body
  }

  fun createCharacter(definition: CharacterDefinition, faction: Faction, position: Vector3): Character {
    val body = createBody(EntityType.character, commonShapes[EntityType.character]!!, position)
    val character = Character(
        id = body.id,
        faction = faction,
        body = body,
        maxHealth = definition.health,
        abilities = definition.abilities.map { Ability(it) }.toMutableList()
    )
    characterTable[body.id] = character
    return character
  }

  fun createMissile(newMissile: NewMissile): Missile {
    val body = createBody(EntityType.missile, commonShapes[EntityType.missile]!!, newMissile.position)
    body.velocity = newMissile.velocity
    val missile = Missile(body.id, body, newMissile.owner, newMissile.range)
    missileTable[body.id] = missile
    return missile
  }

  fun createPlayer(id: Int): Player {
    val position = meta.nodes.first().position + Vector3(0f, 0f, 1f)
    val character = createCharacter(characterDefinitions.player, factions[0], position)
    val player = Player(character, id)
    players.add(player)
    return player
  }
}

class WorldUpdater(val world: World) {

  fun applyPlayerCommands(player: Player, commands: Commands<CommandType>, delta: Float): NewMissile? {
    if (commands.isEmpty())
      return null

    playerMove(world, player.character.body, commands, delta)
    return playerAttack(world, player.character, commands)
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
    world.characters.forEach { updateCharacter(it, delta) }
  }

  fun createMissiles(newMissiles: List<NewMissile>) {
    for (newMissile in newMissiles) {
      world.createMissile(newMissile)
    }
  }

  fun getFinished(): List<Int> {
    return world.missileTable.values
        .filter { isFinished(world, it) }
        .map { it.id }
        .plus(world.characters
            .filter { isFinished(world, it) }
            .map { it.id })
  }

  fun removeFinished(finished: List<Int>) {
    world.missileTable.minusAssign(finished)
    world.bodyTable.minusAssign(finished)
    world.entities.minusAssign(finished)
    world.characterTable.minusAssign(finished)
  }

  fun update(commands: Commands<CommandType>, delta: Float) {
    updateCharacters(delta)
    val aiCharacters = getAiPlayers(world)
    val newMissiles = aiCharacters.mapNotNull { updateEnemy(it) }
        .plus(applyCommands(world.players, commands, delta))

    world.missileTable.values.forEach { updateMissile(world, it, delta) }

    val finished = getFinished()
    removeFinished(finished)

    createMissiles(newMissiles)
  }
}
