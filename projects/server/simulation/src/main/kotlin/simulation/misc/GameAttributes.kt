package simulation.misc

import silentorb.mythic.ent.PropertyInfo
import silentorb.mythic.ent.PropertySchema
import silentorb.mythic.scenery.SceneProperties

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
}

object MarlothProperties {
  const val mine = "mine"
  const val other = "other"
  const val direction = "direction"
  const val showIfSideIsEmpty = "showIfNull"
  const val sideHeight = "sideLevel"
  const val myBiome = "myBiome"
  const val otherBiome = "otherBiome"
}

fun marlothPropertiesSchema(): PropertySchema = mapOf(
    MarlothProperties.other to PropertyInfo(
        manyToMany = true,
    ),
)
