package simulation.changing

import mythic.breeze.Armature
//import rigging.createSkeleton
//import rigging.humanAnimations
import simulation.*

data class InstantiatorConfig(
    var defaultPlayerView: ViewMode = ViewMode.thirdPerson
)

//fun createArmature(): Armature {
//  val bones = createSkeleton()
//  return Armature(
//      bones = bones,
//      originalBones = bones,
//      levels = listOf(),
//      animations = humanAnimations(bones)
//  )
//}

//fun nextId(world: World): Id {
//  val id = world.getAndSetNextId()
//  return id
//}

class Instantiator(
    private val world: World,
    val config: InstantiatorConfig
) {

//  fun addDepiction(depiction: Depiction) {
//    world.depictionTable[depiction.id] = depiction
//  }

//  fun createCharacter(source: NewCharacter): Character {
//    val body = Body(
//        id = source.id,
//        shape = commonShapes[EntityType.character]!!,
//        position = source.position,
//        orientation = Quaternion(),
//        velocity = Vector3(),
//        node = source.node,
//        attributes = characterBodyAttributes,
//        gravity = true
//    )
//    world.bodyTable = world.bodyTable.plus(Pair(body.id, body))

//    val abilities = definition.abilities.map {
//      Ability(
//          id = nextId(world),
//          definition = it
//      )
//    }
//    val character = Character(
//        id = body.id,
//        turnSpeed = Vector2(1.5f, 1f),
//        faction = faction,
//        body = body,
//        health = Resource(definition.health),
//        abilities = abilities
//    )
//    addDepiction(Depiction(
//        body.id,
//        definition.depictionType,
//        DepictionAnimation(0, 0f, createArmature())
//    ))
//    world.characterTable = world.characterTable.plus(Pair(body.id, character))
//    return character
//  }

//  fun createAiCharacter(definition: CharacterDefinition, faction: Faction, position: Vector3, node: Node): Character {
//    val character = createCharacter(definition, faction, position, node)
//    createSpirit(character)
//    return character
//  }

//  fun createSpirit(character: Character): Spirit {
//    val spirit = Spirit(
//        id = character.id,
//        knowledge = Knowledge(
//            character = character,
//            nodes = listOf(),
//            visibleCharacters = listOf()
//        ),
//        pursuit = Pursuit()
//    )
//    world.spiritTable[character.id] = spirit
//    return spirit
//  }

//  fun createPlayer(id: Id): Player {
//    val node = world.meta.nodes.first()
//    val position = node.position// + Vector3(0f, 0f, 1f)
//    val character = createCharacter(characterDefinitions.player, world.factions[0], position, node)
//    val player = Player(character.id, id, config.defaultPlayerView)
//    world.players = world.players.plus(player)
//    return player
//  }

//  fun createFurnishing(depictionType: DepictionType, position: Vector3, node: Node, orientation: Quaternion): Body {
//    val body = Body(
//        id = nextId(),
//        shape = Sphere(0.3f),
//        position = position,
//        orientation = orientation,
//        velocity = Vector3(),
//        node = node,
//        attributes = doodadBodyAttributes,
//        gravity = false
//    )
//    world.bodyTable = world.bodyTable.plus(Pair(body.id, body))
//    addDepiction(Depiction(body.id, depictionType, null))
//    return body
//  }

//  fun addLight(id: Id, light: Light) {
//    world.lights[id] = light
//  }

//  fun createWallLamp(position: Vector3, node: Node, orientation: Quaternion): Body {
//    val body = createFurnishing(DepictionType.wallLamp, position, node, orientation)
//    val light = Light(
//        type = LightType.point,
//        color = Vector4(1f, 1f, 1f, 1f),
//        position = position + Vector3(0f, 0f, 1.6f),
//        direction = Vector4(0f, 0f, 0f, 15f)
//    )
//    addLight(body.id, light)
//    return body
//  }
}