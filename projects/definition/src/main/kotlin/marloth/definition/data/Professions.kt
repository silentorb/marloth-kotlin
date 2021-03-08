package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.SoundId
import marloth.scenery.enums.TextId
import silentorb.mythic.ent.reflectPropertiesMap
import simulation.characters.CharacterDefinition
import simulation.entities.DepictionType

object Professions {
  val soldier = CharacterDefinition(
      name = TextId.id_soldier,
      health = 200,
      accessories = listOf(AccessoryId.shotgun, AccessoryId.graveDigger, AccessoryId.mobility),
      depictionType = DepictionType.child,
      deathSound = SoundId.girlScream,
      damageMultipliers = mapOf()
  )

  val magician = CharacterDefinition(
      name = TextId.id_magician,
      health = 200,
      accessories = listOf(AccessoryId.rocketLauncher, AccessoryId.mobility),
      depictionType = DepictionType.child,
      deathSound = SoundId.girlScream,
      damageMultipliers = mapOf(),
      money = 200,
  )

}

private val debug = CharacterDefinition(
    name = TextId.unnamed,
    health = 20000,
    accessories = listOf(AccessoryId.rocketLauncher),
    depictionType = DepictionType.child,
    speed = 16f,
    deathSound = SoundId.girlScream,
    damageMultipliers = mapOf()
)

fun availableProfessions() = reflectPropertiesMap<CharacterDefinition>(Professions)

fun allProfessions() = availableProfessions() + Pair("debug", debug)
