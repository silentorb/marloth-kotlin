package simulation

import intellect.Spirit
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
  val bodyTable: MutableMap<Id, Body> = mutableMapOf()
  val characterTable: MutableMap<Id, Character> = mutableMapOf()
  val missileTable: MutableMap<Id, Missile> = mutableMapOf()
  val spiritTable: MutableMap<Id, Spirit> = mutableMapOf()
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

  val spirits: MutableCollection<Spirit>
    get() = spiritTable.values

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

  fun createAiCharacter(definition: CharacterDefinition, faction: Faction, position: Vector3): Character {
    val character = createCharacter(definition, faction, position)
    createSpirit(character)
    return character
  }

  fun createSpirit(character: Character): Spirit {
    val spirit = Spirit(character)
    spiritTable[character.id] = spirit
    return spirit
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