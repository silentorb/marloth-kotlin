package simulation.characters

import marloth.scenery.enums.AnimationId
import marloth.scenery.enums.ClientCommand
import marloth.scenery.enums.TextId
import silentorb.mythic.characters.rigs.*
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.scenery.Capsule
import silentorb.mythic.spatial.*
import simulation.accessorize.Accessory
import simulation.accessorize.AccessoryStack
import simulation.combat.general.Destructible
import simulation.combat.general.DestructibleBaseStats
import simulation.entities.*
import simulation.happenings.newPossibleAction
import simulation.main.NewHand
import simulation.misc.Definitions
import simulation.misc.Factions
import simulation.physics.CollisionGroups

fun newCharacter(
    id: Id,
    definitions: Definitions,
    definition: CharacterDefinition,
    faction: Id,
    position: Vector3,
    angle: Float = Pi / 2f
): NewHand {
  val accessories = definition.accessories
      .map { type ->
        val accessoryDefinition = definitions.accessories[type]
        NewHand(
            components = listOfNotNull(
                AccessoryStack(
                    value = Accessory(
                        type = type,
                    ),
                    owner = id,
                    quantity = accessoryDefinition?.charges,
                ),
                newPossibleAction(definitions, type),
            )
        )
      }

  val nextWareId = newIdSource(1L)

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
              energy = definition.health,
              wares = definition.wares.associateBy { nextWareId() },
              availableContracts = definition.availableContracts.associateBy { nextWareId() },
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
              health = definition.health,
              maxHealth = definition.health,
              drainDuration = healthTimeDrainDuration,
              damageMultipliers = definition.damageMultipliers
          ),

          if (definition.wares.any() || definition.availableContracts.any()) {
            Interactable(
                primaryCommand = WidgetCommand(
                    text = TextId.menu_talk,
                    commandType = ClientCommand.showConversationView
                )
            )
          } else
            null,
      )
  )
}

fun newCharacter(nextId: IdSource, definitions: Definitions, definition: CharacterDefinition, transform: Matrix, faction: Id = Factions.neutral): NewHand {
  return newCharacter(nextId(), definitions, definition, faction, transform.translation(), transform.rotation().z)
}

fun newCharacter(nextId: IdSource, definitions: Definitions, definition: CharacterDefinition, graph: Graph, node: Key, faction: Id = Factions.neutral): NewHand {
  val transform = getNodeTransform(graph, node)
  return newCharacter(nextId, definitions, definition, transform, faction)
}