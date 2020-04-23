package marloth.definition

import marloth.scenery.enums.AnimationId
import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.characters.CharacterRig
import silentorb.mythic.characters.ViewMode
import silentorb.mythic.characters.defaultCharacterHeight
import silentorb.mythic.characters.defaultCharacterRadius
import simulation.combat.general.Destructible
import simulation.combat.general.DestructibleBaseStats
import simulation.combat.general.ResourceContainer
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.scenery.Capsule
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3
import simulation.entities.*
import simulation.intellect.Spirit
import simulation.intellect.assessment.newKnowledge
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.Definitions
import simulation.happenings.newPossibleAction

fun newCharacter(nextId: IdSource, character: Id, definitions: Definitions, definition: CharacterDefinition, faction: Id, position: Vector3,
                 angle: Float = Pi / 2f,
                 spirit: Spirit? = null): List<IdHand> {
  val accessories = definition.accessories
      .mapIndexed { index, type ->
        IdHand(
            id = nextId(),
            hand = Hand(
                accessory = Accessory(
                    type = type,
                    owner = character
                ),
                action = newPossibleAction(definitions, type)
            )
        )
      }

  return listOf(
      IdHand(
          id = character,
          hand = Hand(
              ambientAudioEmitter = if (definition.ambientSounds.any())
                AmbientAudioEmitter(
                    delay = position.length() % 2.0
                )
              else
                null,
              animation = CharacterAnimation(
                  animations = listOf(
                      SingleAnimation(
                          animationId = AnimationId.stand.name,
                          animationOffset = 0f
                      )
                  )
              ),
              body = Body(
                  position = position,
                  orientation = Quaternion(),
                  velocity = Vector3()
              ),
              character = Character(
                  definition = definition,
                  faction = faction,
                  sanity = ResourceContainer(100),
                  money = 30,
                  isAlive = true,
                  activeAccessory = accessories.firstOrNull()?.id
              ),
              characterRig = CharacterRig(
                  facingRotation = Vector3(0f, 0f, angle),
                  isActive = true,
                  maxSpeed = definition.maxSpeed,
                  turnSpeed = Vector2(3f, 1f),
                  viewMode = if (spirit == null) ViewMode.firstPerson else ViewMode.firstPerson
              ),
              destructible = Destructible(
                  base = DestructibleBaseStats(
                      health = definition.health,
                      damageMultipliers = definition.damageMultipliers
                  ),
                  health = ResourceContainer(definition.health),
                  damageMultipliers = definition.damageMultipliers
              ),
              collisionShape = CollisionObject(
                  shape = Capsule(defaultCharacterRadius, defaultCharacterHeight)
              ),
              depiction = Depiction(
                  type = definition.depictionType
              ),
              dynamicBody = DynamicBody(
                  gravity = true,
                  mass = 45f,
                  resistance = 4f
              ),
              spirit = spirit,
              knowledge = if (spirit != null)
                newKnowledge()
              else
                null
          )
      )
  ).plus(accessories)
}
