package marloth.scenery.creation

import marloth.scenery.ArmatureId
import marloth.scenery.ArmatureSockets
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import silentorb.mythic.lookinglass.*
import simulation.entities.*
import simulation.main.Deck
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


fun filterDepictions(depictions: Table<Depiction>, player: Id, playerRecord: Player): Table<Depiction> =
//    if (playerRecord.viewMode == ViewMode.firstPerson)
      depictions.filter { it.key != player }
//    else
//      depictions

fun convertSimpleDepiction(deck: Deck, id: Id, mesh: MeshName, texture: TextureName? = null): MeshElement? {
  val body = deck.bodies[id]!!
  val characterRig = deck.characterRigs[id]
  val translate = Matrix.identity.translate(body.position)
  val transform = if (characterRig != null)
    translate.rotate(characterRig.facingQuaternion)
  else
    translate.rotate(body.orientation)

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

fun convertComplexDepiction(deck: Deck, id: Id, depiction: Depiction): ElementGroup {
  val body = deck.bodies[id]!!
  val characterRig = deck.characterRigs[id]!!
  val collisionObject = deck.collisionShapes[id]!!
  val shape = collisionObject.shape
  val verticalOffset = -shape.height / 2f

  val transform = Matrix.identity
      .translate(body.position + Vector3(0f, 0f, verticalOffset))
      .rotate(characterRig.facingQuaternion)
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
    else -> listOf(
        MeshId.personBody,
        MeshId.hogHead,
        MeshId.pants,
        MeshId.shirt
    )
  }

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
      attachments = listOf(
          AttachedMesh(
              socket = ArmatureSockets.rightHand.toString(),
              mesh = MeshElement(
                  id = id,
                  mesh = MeshId.pistol.name,
                  transform = transform
              )
          )
      )
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

fun gatherVisualElements(deck: Deck, player: Id, playerRecord: Player): ElementGroups {
  val (complex, simple) =
      filterDepictions(deck.depictions, player, playerRecord)
          .entries.partition { isComplexDepiction(it.value) }

  val complexElements = complex.map {
    convertComplexDepiction(deck, it.key, it.value)
  }

  val simpleElements =
      simple.mapNotNull {
        convertSimpleDepiction(deck, it.key, it.value)
      }
          .plus(deck.doors.mapNotNull {
            convertSimpleDepiction(deck, it.key, MeshId.prisonDoor.name)
          })

  return complexElements
      .plus(ElementGroup(simpleElements))
}
