package simulation.misc

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.PropertyInfo
import silentorb.mythic.ent.PropertySchema

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
}

enum class BlockRotations {
  all,
  none,
  once
}

val modeTypes = listOf(
    "door"
)

object MarlothProperties {
  const val mine = "mine"
  const val other = "other"
  const val direction = "direction"
  const val showIfSideIsEmpty = "showIfNull"
  const val sideHeight = "sideLevel"
  const val biome = "biome"
  const val blockRotations = "blockRotations"
  const val heightVariant = "heightVariant"
  const val interaction = "interaction"
  const val itemType = "itemType"
  const val modeType = "modeType"
  const val mode = "mode"
}

fun marlothPropertiesSchema(): PropertySchema = mapOf(
    MarlothProperties.biome to PropertyInfo(
        manyToMany = true,
    ),
    MarlothProperties.other to PropertyInfo(
        manyToMany = true,
    ),
    MarlothProperties.showIfSideIsEmpty to PropertyInfo(
        manyToMany = true,
    ),
    MarlothProperties.heightVariant to PropertyInfo(
        manyToMany = true,
    ),
    MarlothProperties.interaction to PropertyInfo(
        manyToMany = true,
    ),
)
