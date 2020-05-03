package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.SoundId
import marloth.scenery.enums.Text
import silentorb.mythic.ent.reflectPropertiesMap
import simulation.characters.CharacterDefinition
import simulation.entities.DepictionType

object Professions {
  val soldier = CharacterDefinition(
      name = Text.id_soldier,
      health = 200,
      accessories = listOf(AccessoryId.shotgun),
      depictionType = DepictionType.child,
      maxSpeed = 5f,
      deathSound = SoundId.girlScream,
      damageMultipliers = mapOf()
  )

  val magician = CharacterDefinition(
      name = Text.id_magician,
      health = 200,
      accessories = listOf(AccessoryId.rocketLauncher, AccessoryId.entangle),
      depictionType = DepictionType.child,
      maxSpeed = 8f,
      deathSound = SoundId.girlScream,
      damageMultipliers = mapOf()
  )

}

private val debug = CharacterDefinition(
    name = Text.unnamed,
    health = 20000,
    accessories = listOf(AccessoryId.rocketLauncher),
    depictionType = DepictionType.child,
    maxSpeed = 8f,
    deathSound = SoundId.girlScream,
    damageMultipliers = mapOf()
)

fun availableProfessions() = reflectPropertiesMap<CharacterDefinition>(Professions)

fun allProfessions() = availableProfessions() + Pair("debug", debug)
