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
}

object MarlothProperties {
  const val mine = "mine"
  const val other = "other"
  const val direction = "direction"
}

fun marlothPropertiesSchema(): PropertySchema = mapOf(
    MarlothProperties.other to PropertyInfo(
        manyToMany = true,
    ),
)
