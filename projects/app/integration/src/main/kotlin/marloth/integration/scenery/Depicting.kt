package marloth.integration.scenery

import marloth.scenery.enums.ArmatureId
import marloth.scenery.enums.ArmatureSockets
import marloth.scenery.enums.MeshId
import silentorb.mythic.accessorize.getAccessories
import silentorb.mythic.characters.CharacterRig
import silentorb.mythic.characters.ThirdPersonRig
import silentorb.mythic.characters.ViewMode
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.lookinglass.*
import silentorb.mythic.physics.Body
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.Deck
import simulation.misc.Definitions
import kotlin.math.floor

val simplePainterMap = MeshId.values().mapNotNull { meshId ->
  val depictionType = DepictionType.values().firstOrNull { it.name == meshId.name }
  if (depictionType != null)
    Pair(depictionType, meshId)
  else
    null
}.associate { it }
    .plus(
        mapOf(
            DepictionType.child to MeshId.personBody
        )
    )


fun filterDepictions(depictions: Table<Depiction>, player: Id, characterRig: CharacterRig): Table<Depiction> =
    if (characterRig.viewMode == ViewMode.firstPerson)
      depictions.filter { it.key != player }
    else
      depictions

fun depictionTransform(bodies: Table<Body>, characterRigs: Table<CharacterRig>, id: Id): Matrix {
  val body = bodies[id]!!
  val characterRig = characterRigs[id]
  val translate = Matrix.identity.translate(body.position)
  return if (characterRig != null)
    translate.rotate(characterRig.facingOrientation)
  else
    translate.rotate(body.orientation)

}

fun convertSimpleDepiction(deck: Deck, id: Id, mesh: MeshName, texture: TextureName? = null): MeshElement? {
  val body = deck.bodies[id]!!
  val transform = depictionTransform(deck.bodies, deck.characterRigs, id)

  val material = if (texture != null)
    Material(texture = texture, shading = true)
  else
    null

  return MeshElement(
      id = id,
      mesh = mesh,
      transform = transform.scale(body.scale),
      material = material
  )
}

fun convertSimpleDepiction(deck: Deck, id: Id, depiction: Depiction): MeshElement? {
  val mesh = if (depiction.type == DepictionType.staticMesh)
    depiction.mesh
  else
    simplePainterMap[depiction.type]?.name

  if (mesh == null)
    return null

  return convertSimpleDepiction(deck, id, mesh, depiction.texture)
}

fun convertComplexDepiction(definitions: Definitions, deck: Deck, id: Id, depiction: Depiction): ElementGroup {
  val body = deck.bodies[id]!!
  val characterRig = deck.characterRigs[id]!!
  val collisionObject = deck.collisionObjects[id]!!
  val shape = collisionObject.shape
  val verticalOffset = -shape.height / 2f

  val transform = Matrix.identity
      .translate(body.position + Vector3(0f, 0f, verticalOffset))
      .rotateZ(characterRig.facingRotation.z)
      .rotateZ(Pi / 2f)

  val animations = deck.animations[id]!!.animations.map {
    ElementAnimation(
        animationId = it.animationId,
        timeOffset = it.animationOffset,
        strength = it.strength
    )
  }
  val meshes = when {
    depiction.type == DepictionType.child -> listOf(
        MeshId.personBody,
        MeshId.pants,
        MeshId.shirt,
        MeshId.pumpkinHead
    )
    depiction.type == DepictionType.sentinel -> listOf(
        MeshId.personBody,
        MeshId.pants,
        MeshId.shirt,
        MeshId.sentinelHead
    )
    depiction.type == DepictionType.hound -> listOf(
        MeshId.personBody,
        MeshId.pants,
        MeshId.shirt
    )
    else -> listOf(
        MeshId.personBody,
        MeshId.hogHead,
        MeshId.pants,
        MeshId.shirt
    )
  }

  val accessories = getAccessories(deck.accessories, id)

  return ElementGroup(
      meshes = meshes
          .map {
            MeshElement(
                id = id,
                mesh = it.name,
                transform = transform
            )
          },
      armature = ArmatureId.person.name,
      animations = animations,
      attachments = accessories
          .mapNotNull { (_, accessoryRecord) ->
            val accessoryType = definitions.accessories[accessoryRecord.type]
            val mesh = accessoryType?.equippedMesh
            if (mesh != null)
              AttachedMesh(
                  socket = ArmatureSockets.rightHand.toString(),
                  mesh = MeshElement(
                      id = id,
                      mesh = mesh,
                      transform = transform
                  )
              )
            else
              null
          }
  )
}

private val complexDepictions = setOf(
    DepictionType.child,
    DepictionType.person
)

val isComplexDepiction = { depiction: Depiction ->
  complexDepictions.contains(depiction.type)
}

fun gatherParticleElements(deck: Deck, cameraPosition: Vector3): ElementGroups {
  return deck.particleEffects
      .entries.sortedByDescending { cameraPosition.distance(deck.bodies[it.key]!!.position) }
      .map { (_, particleEffect) ->
        ElementGroup(
            billboards = particleEffect.particles.map { particle ->
              TexturedBillboard(
                  texture = particle.texture,
                  position = particle.position,
                  scale = particle.size,
                  color = particle.color,
                  step = floor(particle.animationStep * 16f).toInt()
              )
            }
        )
      }
}

fun depictionSwitch(definitions: Definitions, deck: Deck, player: Id, depiction: Id, depictionRecord: Depiction): ElementGroup? {
  return when (depictionRecord.type) {
    DepictionType.hound,
    DepictionType.sentinel -> convertComplexDepiction(definitions, deck, depiction, depictionRecord)
    else -> null
  }
}

fun gatherVisualElements(definitions: Definitions, deck: Deck, player: Id, characterRig: CharacterRig): ElementGroups {
  val initialPass = deck.depictions.map { (id, depiction) ->
    Pair(id, depictionSwitch(definitions, deck, player, id, depiction))
  }
  val initial = initialPass.mapNotNull { it.second }
  val remaining = deck.depictions.minus(initialPass.filter { it.second != null }.map { it.first })
  val (complex, simple) =
      filterDepictions(remaining, player, characterRig)
          .entries.partition { isComplexDepiction(it.value) }

  val complexElements = complex.map {
    convertComplexDepiction(definitions, deck, it.key, it.value)
  }

  val simpleElements =
      simple.mapNotNull {
        convertSimpleDepiction(deck, it.key, it.value)
      }
          .plus(deck.doors.mapNotNull {
            convertSimpleDepiction(deck, it.key, MeshId.prisonDoor.name)
          })

  return initial + complexElements + ElementGroup(simpleElements)
}
