package simulation.misc

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
  val item = "item"

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

object MarlothProperties {
  const val mine = "mine"
  const val other = "other"
  const val direction = "direction"
  const val showIfSideIsEmpty = "showIfNull"
  const val sideHeight = "sideLevel"
  const val biome = "biome"
  const val blockRotations = "blockRotations"
  const val heightVariant = "heightVariant"
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
)
