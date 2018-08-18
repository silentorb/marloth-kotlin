package simulation.changing

import colliding.Sphere
import intellect.Knowledge
import intellect.Pursuit
import intellect.Spirit
import mythic.breeze.Armature
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import org.joml.plus
import physics.Body
import physics.BodyAttributes
import physics.commonShapes
import rigging.createSkeleton
import rigging.humanAnimations
import scenery.*
import simulation.*
import simulation.Id

data class InstantiatorConfig(
    var defaultPlayerView: ViewMode = ViewMode.thirdPerson
)

fun createArmature(): Armature {
  val bones = createSkeleton()
  return Armature(
      bones = bones,
      originalBones = bones,
      animations = humanAnimations(bones)
  )
}

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

  fun createBody(type: EntityType, shape: colliding.Shape, position: Vector3, node: Node, attributes: BodyAttributes,
                 orientation: Quaternion = Quaternion()): Body {
    val entity = createEntity(type)
    val body = Body(
        id = entity.id,
        shape = shape,
        position = position,
        orientation = orientation,
        velocity = Vector3(),
        node = node,
        attributes = attributes
    )
    world.bodyTable[entity.id] = body
    return body
  }

  fun addDepiction(depiction: Depiction) {
    world.depictionTable[depiction.id] = depiction
  }

  fun createCharacter(definition: CharacterDefinition, faction: Faction, position: Vector3, node: Node): Character {
    val body = createBody(EntityType.character, commonShapes[EntityType.character]!!, position, node, characterBodyAttributes)
    val character = Character(
        id = body.id,
        faction = faction,
        body = body,
        health = Resource(definition.health),
        abilities = definition.abilities.map { Ability(it) }.toMutableList()
    )
    addDepiction(Depiction(
        body.id,
        definition.depictionType,
        DepictionAnimation(0, 0f, createArmature())
    ))
    world.characterTable[body.id] = character
    return character
  }

  fun createAiCharacter(definition: CharacterDefinition, faction: Faction, position: Vector3, node: Node): Character {
    val character = createCharacter(definition, faction, position, node)
    createSpirit(character)
    return character
  }

  fun createSpirit(character: Character): Spirit {
    val spirit = Spirit(
        id = character.id,
        goals = listOf(),
        knowledge = Knowledge(
            character = character,
            nodes = listOf(),
            visibleCharacters = listOf()
        ),
        pursuit = Pursuit()
    )
    world.spiritTable[character.id] = spirit
    return spirit
  }

  fun createMissile(newMissile: NewMissile): Missile {
    val shape = commonShapes[EntityType.missile]!!
    val body = createBody(EntityType.missile, shape, newMissile.position, newMissile.node, missileBodyAttributes)
    body.velocity = newMissile.velocity
    val missile = Missile(body.id, body, newMissile.owner, newMissile.range)
    world.missileTable[body.id] = missile
    addDepiction(Depiction(body.id, DepictionType.missile, null))
    return missile
  }

  fun createPlayer(id: Id): Player {
    val node = world.meta.nodes.first()
    val position = node.position// + Vector3(0f, 0f, 1f)
    val character = createCharacter(characterDefinitions.player, world.factions[0], position, node)
    val player = Player(character.id, id, config.defaultPlayerView)
    world.players = world.players.plus(player)
    return player
  }

  fun createFurnishing(depictionType: DepictionType, position: Vector3, node: Node, orientation: Quaternion): Body {
    val body = createBody(EntityType.furnishing, Sphere(0.3f), position, node, doodadBodyAttributes, orientation)
    addDepiction(Depiction(body.id, depictionType, null))
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