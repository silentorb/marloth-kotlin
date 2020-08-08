package marloth.integration.scenery

import marloth.clienting.menus.textStyles
import marloth.definition.data.animationPlaceholders
import marloth.scenery.enums.*
import simulation.accessorize.AccessoryName
import simulation.accessorize.getAccessories
import silentorb.mythic.characters.rigs.CharacterRig
import silentorb.mythic.characters.rigs.ViewMode
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.ent.reflectProperties
import silentorb.mythic.lookinglass.*
import silentorb.mythic.performing.isMarkerTriggered
import silentorb.mythic.physics.Body
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.scenery.Shape
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Vector3
import simulation.combat.spatial.executeMarker
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.Deck
import simulation.misc.Definitions
import simulation.updating.simulationDelta
import kotlin.math.floor

val simplePainterMap = reflectProperties<String>(MeshId).mapNotNull { meshId ->
  val depictionType = DepictionType.values().firstOrNull { it.name == meshId }
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
  val transform = Matrix.identity.translate(body.position)
  return if (characterRig != null)
    transform.rotate(characterRig.facingOrientation)
  else
    transform.rotate(body.orientation)
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
      material = material,
      location = body.position
  )
}

fun convertSimpleDepiction(deck: Deck, id: Id, depiction: Depiction): MeshElement? {
  val mesh = if (depiction.type == DepictionType.staticMesh)
    depiction.mesh
  else
    simplePainterMap[depiction.type]

  if (mesh == null)
    return null

  return convertSimpleDepiction(deck, id, mesh, depiction.texture)
}

fun accessoryDebugName(definitions: Definitions, accessoryType: AccessoryName): String {
  val definition = definitions.accessories[accessoryType]
  return if (definition == null)
    "???"
  else if (definition.name == Text.unnamed)
    definition.debugName ?: "???"
  else
    definitions.textLibrary(definition.name)
}

fun getDebugTextBillboard(definitions: Definitions, deck: Deck, actor: Id, footPosition: Vector3, shape: Shape): List<TextBillboard> =
    if (!getDebugBoolean("DRAW_PERFORMANCE_TEXT"))
      listOf()
    else {
      val performance = deck.performances.entries.firstOrNull { it.value.target == actor }
      val performanceBillboard = if (performance != null) {
        val action = performance.value.sourceAction
        val accessory = deck.accessories[action]
        if (accessory != null) {
          val timer = deck.timersFloat.getValue(performance.key)
          val animation = definitions.animations.getValue(performance.value.animation)
          val isTriggered = isMarkerTriggered(timer.duration, animation, simulationDelta * 6f)(executeMarker)
          val suffix = if (isTriggered)
            "!"
          else
            ""

          TextBillboard(
              content = accessoryDebugName(definitions, accessory.type) + suffix,
              position = footPosition + Vector3(0f, 0f, shape.height + 0.1f),
              style = textStyles.smallWhite,
              depthOffset = -0.01f
          )
        } else
          null
      } else
        null

      val accessory = getAccessories(deck.accessories, actor)
          .filterKeys { deck.timersFloat.containsKey(it) }
          .values.firstOrNull()

      val modifierBillboard = if (accessory != null) {
        TextBillboard(
            content = accessoryDebugName(definitions, accessory.type),
            position = footPosition + Vector3(0f, 0f, shape.height - 0.3f),
            style = textStyles.smallGray,
            depthOffset = -0.01f
        )
      } else
        null

      listOfNotNull(performanceBillboard, modifierBillboard)
    }

fun convertCharacterDepiction(definitions: Definitions, deck: Deck, id: Id, depiction: Depiction): ElementGroup {
  val body = deck.bodies[id]!!
  val characterRig = deck.characterRigs[id]!!
  val collisionObject = deck.collisionObjects[id]!!
  val shape = collisionObject.shape
  val verticalOffset = -shape.height / 2f
  val footPosition = body.position + Vector3(0f, 0f, verticalOffset)

  val transform = Matrix.identity
      .translate(footPosition)
      .rotateZ(characterRig.facingRotation.x)
      .rotateZ(Pi / 2f)

  val animations = deck.animations[id]!!.animations.map {
    val animationId = if (animationPlaceholders().containsKey(it.animationId))
      AnimationId.stand
    else
      it.animationId

    ElementAnimation(
        animationId = animationId,
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
                mesh = it,
                transform = transform,
                location = body.position
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
          },
      textBillboards = getDebugTextBillboard(definitions, deck, id, footPosition, shape)
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
    DepictionType.sentinel -> convertCharacterDepiction(definitions, deck, depiction, depictionRecord)
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
    convertCharacterDepiction(definitions, deck, it.key, it.value)
  }

  val simpleElements =
      simple.mapNotNull {
        convertSimpleDepiction(deck, it.key, it.value)
      }
          .plus(deck.doors.mapNotNull {
            convertSimpleDepiction(deck, it.key, MeshId.prisonDoor)
          })

  return initial + complexElements + ElementGroup(simpleElements)
}
