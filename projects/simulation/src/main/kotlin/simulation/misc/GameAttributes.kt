package simulation.misc

import silentorb.mythic.ent.PropertyInfo
import silentorb.mythic.ent.PropertySchema
import silentorb.mythic.ent.reflectProperties

object Entities {
  val farmer = "farmer"
  val grocer = "grocer"
  val monsterSpawn = "monsterSpawn"
}

object GameAttributes {
  val playerSpawn = "playerSpawn"
  val resource = "resource"
  val sleepable = "sleepable"
  val victoryZone = "victoryZone"

  // Block side related
  val blockSide = "blockSide"
  val anyBiome = "anyBiome"
  val lockedRotation = "lockedRotation"
  val lightDistribution = "lightDistribution"
  val showIfSideIsEmpty = "showIfSideIsEmpty"
  val heteroBlock = BlockAttributes.hetero

  val wallSlot = "wallSlot"
  val isEssentialSide = "isEssentialSide"
  val intrinsicReplenishment = "intrinsicReplenishment"
  val tryToFill = "tryToFill"
}

object DistAttributes {
  val floor = "distFloor"
  val food = "distFood"
  val light = "distLight"
  val wall = "distWall"
}

val distAttributes = reflectProperties<String>(DistAttributes)

enum class BlockRotations {
  all,
  none,
  once
}

val modeTypes = listOf(
    "door"
)

object GameProperties {
  const val mine = "mine"
  const val other = "other"
  const val direction = "direction"
  const val showIfSideIsEmpty = "showIfNull"
  const val sideHeight = "sideLevel"
  const val biome = "biome"
  const val blockRotations = "blockRotations"
  const val heightVariant = "heightVariant"
  const val interaction = "interaction"
  const val onInteract = "onInteract"
  const val itemType = "itemType"
  const val removeOnEmpty = "removeOnEmpty"
  const val modeType = "modeType"
  const val depiction = "depiction"
  const val mode = "mode"
  const val rarity = "rarity"
  const val maxQuantity = "maxQuantity"

  const val mass = "mass"
//  const val resistance = "resistance"
//  const val gravity = "gravity"
}

fun marlothPropertiesSchema(): PropertySchema = mapOf(
    GameProperties.biome to PropertyInfo(
        manyToMany = true,
    ),
    GameProperties.other to PropertyInfo(
        manyToMany = true,
    ),
    GameProperties.showIfSideIsEmpty to PropertyInfo(
        manyToMany = true,
    ),
    GameProperties.heightVariant to PropertyInfo(
        manyToMany = true,
    ),
)
