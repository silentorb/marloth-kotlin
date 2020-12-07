package simulation.characters

import marloth.scenery.enums.AnimationId
import marloth.scenery.enums.ClientCommand
import marloth.scenery.enums.Text
import simulation.accessorize.Accessory
import silentorb.mythic.characters.rigs.*
import simulation.combat.general.Destructible
import simulation.combat.general.DestructibleBaseStats
import simulation.combat.general.ResourceContainer
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.newIdSource
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.scenery.Capsule
import silentorb.mythic.spatial.*
import simulation.entities.*
import simulation.intellect.Spirit
import simulation.intellect.assessment.newKnowledge
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.Definitions
import simulation.happenings.newPossibleAction
import simulation.main.NewHand
import simulation.physics.CollisionGroups

fun newCharacter(nextId: IdSource, character: Id, definitions: Definitions, profession: ProfessionId, faction: Id, position: Vector3,
                 angle: Float = Pi / 2f,
                 spirit: Spirit? = null): List<IdHand> {
  val definition = definitions.professions[profession]!!
  val accessories = definition.accessories
      .map { type ->
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
              body = Body(
                  position = position,
                  velocity = Vector3(),
                  orientation = Quaternion()
              ),
              animation = CharacterAnimation(
                  animations = listOf(
                      SingleAnimation(
                          animationId = AnimationId.stand,
                          animationOffset = 0f
                      )
                  )
              ),
              dynamicBody = DynamicBody(
                  gravity = true,
                  mass = 45f,
                  resistance = 4f
              ),
              character = Character(
                  faction = faction,
                  isAlive = true,
                  definition = definition,
              ),
              characterRig = CharacterRig(
                  facingRotation = Vector2(angle, 0f),
                  facingOrientation = characterRigOrentation(Vector2(angle, 0f)),
                  viewMode = ViewMode.firstPerson
              ),
              collisionShape = CollisionObject(
                  shape = Capsule(defaultCharacterRadius, defaultCharacterHeight),
                  groups = CollisionGroups.dynamic,
                  mask = CollisionGroups.standardMask
              ),
              depiction = Depiction(
                  type = definition.depictionType
              ),
              destructible = Destructible(
                  base = DestructibleBaseStats(
                      health = definition.health,
                      damageMultipliers = definition.damageMultipliers
                  ),
                  health = ResourceContainer(definition.health),
                  damageMultipliers = definition.damageMultipliers
              ),
              knowledge = if (spirit != null)
                newKnowledge()
              else
                null,
              spirit = spirit,
              thirdPersonRig = if (spirit == null)
                newThirdPersonRig(position, angle)
              else
                null
          )
      )
  ).plus(accessories)
}

fun newCharacter2(id: Id, definitions: Definitions, definition: CharacterDefinition, faction: Id, position: Vector3,
                  angle: Float = Pi / 2f,
                  spirit: Spirit? = null): NewHand {
  val accessories = definition.accessories
      .map { type ->
        NewHand(
            components = listOfNotNull(
                Accessory(
                    type = type,
                    owner = id,
                ),
                newPossibleAction(definitions, type),
            )
        )
      }

  return NewHand(
      id = id,
      children = accessories,
      components = listOfNotNull(
          if (definition.ambientSounds.any())
            AmbientAudioEmitter(
                delay = position.length() % 2.0
            )
          else
            null,
          Body(
              position = position,
              velocity = Vector3(),
              orientation = Quaternion()
          ),
          CharacterAnimation(
              animations = listOf(
                  SingleAnimation(
                      animationId = AnimationId.stand,
                      animationOffset = 0f
                  )
              )
          ),
          DynamicBody(
              gravity = true,
              mass = 45f,
              resistance = 4f
          ),
          Character(
              faction = faction,
              isAlive = true,
              definition = definition,
              money = definition.money,
          ),
          CharacterRig(
              facingRotation = Vector2(angle, 0f),
              facingOrientation = characterRigOrentation(Vector2(angle, 0f)),
              viewMode = ViewMode.firstPerson
          ),
          CollisionObject(
              shape = Capsule(defaultCharacterRadius, defaultCharacterHeight),
              groups = CollisionGroups.dynamic,
              mask = CollisionGroups.standardMask
          ),
          Depiction(
              type = definition.depictionType
          ),
          Destructible(
              base = DestructibleBaseStats(
                  health = definition.health,
                  damageMultipliers = definition.damageMultipliers
              ),
              health = ResourceContainer(definition.health),
              damageMultipliers = definition.damageMultipliers
          ),
          if (spirit != null)
            newKnowledge()
          else
            null,
          spirit,
          if (spirit == null)
            newThirdPersonRig(position, angle)
          else
            null,
          if (definition.wares.any()) {
            val nextWareId = newIdSource(1)
            Vendor(
                wares = definition.wares.associateBy { nextWareId() }
            )
          } else
            null,
          if (definition.wares.any()) {
            Interactable(
                primaryCommand = WidgetCommand(
                    text = Text.menu_talk,
                    clientCommand = ClientCommand.showMerchantView
                )
            )
          } else
            null,
      )
  )
}
