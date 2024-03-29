package marloth.definition.data

import marloth.scenery.enums.*
import simulation.characters.CharacterDefinition
import simulation.characters.fieldOfView360
import simulation.entities.DepictionType

object AccessoryPools {
  val common: List<String> = listOf(
      AccessoryIdOld.pistol,
      AccessoryIdOld.spiritRocketLauncher,
      AccessoryIdOld.entangle,
  )

  val mobile: List<String> = common + listOf(
      AccessoryIdOld.claws,
      AccessoryIdOld.dash,
      AccessoryIdOld.pistol,
  )
}

fun monsterDefinitions(): Map<String, CharacterDefinition> =
    mapOf(
        CreatureId.hogMan to CharacterDefinition(
            name = TextId.unnamed,
            health = 100,
            accessoryPool = AccessoryPools.mobile,
            depictionType = DepictionType.person,
            runSpeed = 2.5f,
            deathSound = Sounds.hogDeath,
            ambientSounds = listOf(
            )
        ),
        CreatureId.sentinel to CharacterDefinition(
            name = TextId.unnamed,
            health = 100,
            accessoryPool = AccessoryPools.common,
            depictionType = DepictionType.sentinel,
            runSpeed = 0f,
            deathSound = null,
            ambientSounds = listOf(),
            fieldOfView = fieldOfView360
        ),
        CreatureId.hound to CharacterDefinition(
            name = TextId.unnamed,
            health = 100,
            accessoryPool = AccessoryPools.mobile,
            depictionType = DepictionType.hound,
            runSpeed = 2.5f,
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

