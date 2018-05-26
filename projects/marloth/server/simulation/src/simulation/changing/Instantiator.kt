package simulation.changing

import colliding.Sphere
import intellect.Spirit
import intellect.SpiritState
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import org.joml.plus
import physics.Body
import physics.commonShapes
import scenery.Depiction
import scenery.DepictionType
import scenery.Light
import scenery.LightType
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

  fun createBody(type: EntityType, shape: colliding.Shape, position: Vector3, node: Node, orientation: Quaternion = Quaternion()): Body {
    val entity = createEntity(type)
    val body = Body(entity.id, shape, position, orientation, Vector3(), node)
    world.bodyTable[entity.id] = body
    return body
  }

  fun createDepiction(id: Id, type: DepictionType): Depiction {
    val depiction = Depiction(id, type)
    world.depictionTable[id] = depiction
    return depiction
  }

  fun createCharacter(definition: CharacterDefinition, faction: Faction, position: Vector3, node: Node): Character {
    val body = createBody(EntityType.character, commonShapes[EntityType.character]!!, position, node)
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

  fun createAiCharacter(definition: CharacterDefinition, faction: Faction, position: Vector3, node: Node): Character {
    val character = createCharacter(definition, faction, position, node)
    createSpirit(character)
    return character
  }

  fun createSpirit(character: Character): Spirit {
    val state = SpiritState()
    val spirit = Spirit(character, state)
    world.spiritTable[character.id] = spirit
    return spirit
  }

  fun createMissile(newMissile: NewMissile): Missile {
    val body = createBody(EntityType.missile, commonShapes[EntityType.missile]!!, newMissile.position, newMissile.node)
    body.velocity = newMissile.velocity
    val missile = Missile(body.id, body, newMissile.owner, newMissile.range)
    world.missileTable[body.id] = missile
    createDepiction(body.id, DepictionType.missile)
    return missile
  }

  fun createPlayer(id: Id): Player {
    val node = world.meta.nodes.first()
    val position = node.position// + Vector3(0f, 0f, 1f)
    val character = createCharacter(characterDefinitions.player, world.factions[0], position, node)
    val player = Player(character, id, config.defaultPlayerView)
    world.players.add(player)
    return player
  }

  fun createFurnishing(depictionType: DepictionType, position: Vector3, node: Node, orientation: Quaternion): Body {
    val body = createBody(EntityType.furnishing, Sphere(0.3f), position, node, orientation)
    createDepiction(body.id, depictionType)
    return body
  }

  fun addLight(id: Id, light: Light) {
    world.lights[id] = light
  }

  fun createWallLamp(position: Vector3, node: Node, orientation: Quaternion): Body {
    val body = createFurnishing(DepictionType.wallLamp, position, node, orientation)
    val light = Light(
        type = LightType.point,
        color = Vector4(1f, 1f, 1f, 1f),
        position = position + Vector3(0f, 0f, 1.6f),
        direction = Vector4(0f, 0f, 0f, 15f)
    )
    addLight(body.id, light)
    return body
  }
}