package marloth.definition.data

import marloth.scenery.enums.*
import simulation.characters.CharacterDefinition
import simulation.characters.fieldOfView360
import simulation.entities.DepictionType

fun monsterDefinitions(): Map<String, CharacterDefinition> =
    mapOf(
        CreatureId.hogMan to CharacterDefinition(
            name = TextId.unnamed,
            health = 100,
            accessories = listOf(AccessoryIdOld.pistol, AccessoryIdOld.mobility),
            depictionType = DepictionType.person,
            speed = 2.5f,
            deathSound = SoundId.hogDeath,
            ambientSounds = listOf(
            )
        ),
        CreatureId.sentinel to CharacterDefinition(
            name = TextId.unnamed,
            health = 100,
            accessories = listOf(AccessoryIdOld.spiritRocketLauncher, AccessoryIdOld.entangle),
            depictionType = DepictionType.sentinel,
            speed = 0f,
            deathSound = null,
            ambientSounds = listOf(),
            fieldOfView = fieldOfView360
        ),
        CreatureId.hound to CharacterDefinition(
            name = TextId.unnamed,
            health = 100,
            accessories = listOf(AccessoryIdOld.claws, AccessoryIdOld.dash, AccessoryIdOld.mobility),
            depictionType = DepictionType.hound,
            speed = 5f,
            deathSound = null,
            ambientSounds = listOf()
        )
    )
//        .flatMap { (key, definition) ->
//          (1..maxCharacterLevel).map { level ->
//            val health = definition.health
//            Pair("$key$level", definition.copy(
//                level = level,
//                health = health * level,
//                damageMultipliers = mapOf(
//                    DamageTypes.physical to 100 * level
//                )
//            ))
//          }
//        }
//        .associate { it }

