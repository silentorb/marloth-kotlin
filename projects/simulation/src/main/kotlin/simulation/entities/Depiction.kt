package simulation.entities

import silentorb.mythic.ent.reflectProperties
import silentorb.mythic.lookinglass.Material
import silentorb.mythic.scenery.MeshName

object DepictionType {
   val berryBush = "berryBush"
   val child = "child"
   val hound = "hound"
   val person = "person"
   val sentinel = "sentinel"
   val staticMesh = "staticMesh"
}

val depictionTypes = reflectProperties<String>(DepictionType)

data class Depiction(
    val type: String = DepictionType.staticMesh,
    val mesh: MeshName? = null,
    val material: Material? = null,
)
