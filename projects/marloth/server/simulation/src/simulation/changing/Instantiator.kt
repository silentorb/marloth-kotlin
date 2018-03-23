package simulation.changing

import intellect.Spirit
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.plus
import scenery.Depiction
import scenery.DepictionType
import simulation.*

data class InstantiatorConfig(
    var defaultPlayerView: ViewMode = ViewMode.topDown
)

class Instantiator(
    private val world: World,
    val config: InstantiatorConfig
) {

  fun createEntity(type: EntityType): Entity {
    val id = world.getAndSetNextId()
    val entity = Entity(id, type)
    world.entities[id] = entity
    return entity
  }

  fun createBody(type: EntityType, shape: collision.Shape, position: Vector3): Body {
    val entity = createEntity(type)
    val body = Body(entity.id, shape, position, Quaternion(), Vector3())
    world.bodyTable[entity.id] = body
    return body
  }

  fun createDepiction(id: Id, type: DepictionType): Depiction {
    val depiction = Depiction(id, type)
    world.depictionTable[id] = depiction
    return depiction
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
    createDepiction(body.id, definition.depictionType)
    world.characterTable[body.id] = character
    return character
  }

  fun createAiCharacter(definition: CharacterDefinition, faction: Faction, position: Vector3): Character {
    val character = createCharacter(definition, faction, position)
    createSpirit(character)
    return character
  }

  fun createSpirit(character: Character): Spirit {
    val spirit = Spirit(character)
    world.spiritTable[character.id] = spirit
    return spirit
  }

  fun createMissile(newMissile: NewMissile): Missile {
    val body = createBody(EntityType.missile, commonShapes[EntityType.missile]!!, newMissile.position)
    body.velocity = newMissile.velocity
    val missile = Missile(body.id, body, newMissile.owner, newMissile.range)
    world.missileTable[body.id] = missile
    createDepiction(body.id, DepictionType.missile)
    return missile
  }

  fun createPlayer(id: Int): Player {
    val position = world.meta.nodes.first().position// + Vector3(0f, 0f, 1f)
    val character = createCharacter(characterDefinitions.player, world.factions[0], position)
    val player = Player(character, id, config.defaultPlayerView)
    world.players.add(player)
    return player
  }
}