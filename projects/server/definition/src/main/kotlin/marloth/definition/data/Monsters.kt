package marloth.definition.data

import marloth.scenery.enums.*
import simulation.characters.CharacterDefinition
import simulation.characters.fieldOfView360
import simulation.characters.maxCharacterLevel
import simulation.entities.DepictionType

fun monsterDefinitions(): Map<String, CharacterDefinition> =
    mapOf(
        CreatureId.hogMan to CharacterDefinition(
            name = Text.unnamed,
            health = 100,
            accessories = listOf(AccessoryId.pistol),
            depictionType = DepictionType.person,
            speed = 2.5f,
            deathSound = SoundId.hogDeath,
            ambientSounds = listOf(
            )
        ),
        CreatureId.sentinel to CharacterDefinition(
            name = Text.unnamed,
            health = 100,
            accessories = listOf(AccessoryId.rocketLauncher),
            depictionType = DepictionType.sentinel,
            speed = 0f,
            deathSound = null,
            ambientSounds = listOf(),
            fieldOfView = fieldOfView360
        ),
        CreatureId.hound to CharacterDefinition(
            name = Text.unnamed,
            health = 100,
            accessories = listOf(AccessoryId.claws, AccessoryId.dash),
            depictionType = DepictionType.hound,
            speed = 4f,
            deathSound = null,
            ambientSounds = listOf()
        )
    )
        .flatMap { (key, definition) ->
          (1..maxCharacterLevel).map { level ->
            val health = definition.health
            Pair("$key$level", definition.copy(
                level = level,
                health = health * level,
                damageMultipliers = mapOf(
                    DamageTypes.physical to 100 * level
                )
            ))
          }
        }
        .associate { it }

