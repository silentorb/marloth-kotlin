package marloth.definition.data

import marloth.scenery.enums.AccessoryIdOld
import marloth.scenery.enums.SoundId
import marloth.scenery.enums.TextId
import silentorb.mythic.ent.reflectPropertiesMap
import simulation.characters.CharacterDefinition
import simulation.entities.DepictionType

object Professions {
  val soldier = CharacterDefinition(
      name = TextId.id_soldier,
      health = 200,
      accessories = listOf(AccessoryIdOld.shotgun, AccessoryIdOld.graveDigger, AccessoryIdOld.mobility),
      depictionType = DepictionType.child,
      deathSound = SoundId.girlScream,
      damageMultipliers = mapOf()
  )

  val magician = CharacterDefinition(
      name = TextId.id_magician,
      health = 200,
      accessories = listOf(AccessoryIdOld.rocketLauncher, Accessories.shadowSpirit),
      depictionType = DepictionType.child,
      deathSound = SoundId.girlScream,
      damageMultipliers = mapOf(),
      money = 200,
      runSpeed = 6f
  )

}

private val debug = CharacterDefinition(
    name = TextId.unnamed,
    health = 20000,
    accessories = listOf(AccessoryIdOld.rocketLauncher),
    depictionType = DepictionType.child,
    runSpeed = 16f,
    deathSound = SoundId.girlScream,
    damageMultipliers = mapOf()
)

fun availableProfessions() = reflectPropertiesMap<CharacterDefinition>(Professions)

fun allProfessions() = availableProfessions() + Pair("debug", debug)
