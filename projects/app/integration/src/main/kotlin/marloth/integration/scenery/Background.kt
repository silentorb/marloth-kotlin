package marloth.integration.scenery

import marloth.integration.misc.BackgroundTextureId
import silentorb.mythic.ent.Table
import silentorb.mythic.spatial.*
import silentorb.mythic.lookinglass.ElementGroup
import silentorb.mythic.lookinglass.ElementGroups
import silentorb.mythic.lookinglass.Material
import silentorb.mythic.lookinglass.MeshElement
import marloth.scenery.enums.MeshId
import silentorb.mythic.timing.FloatCycle

fun createBackgroundSphere(texture: BackgroundTextureId, cameraPosition: Vector3, orientation: Quaternion = Quaternion()) =
    MeshElement(
        id = 1,
        mesh = MeshId.skySphere,
        transform = Matrix.identity
            .translate(cameraPosition)
            .rotate(orientation)
            .scale(100f),
        material = Material(
            color = Vector4(1f, 1f, 1f, 1f),
            texture = texture.name,
            shading = false
        )
    )

fun gatherBackground(cycles: Table<FloatCycle>, cameraPosition: Vector3): ElementGroups {
  return listOf(ElementGroup(
      meshes = listOf(
          Pair(BackgroundTextureId.backgroundNightSky, Quaternion()),
          Pair(BackgroundTextureId.backgroundClouds, Quaternion().rotateZ(cycles.values.first().value * Pi * 2f)),
          Pair(BackgroundTextureId.backgroundClouds, Quaternion()
              .rotateX(Pi)
              .rotateZ(cycles.values.drop(1).first().value * Pi * 2f))

      ).map { createBackgroundSphere(it.first, cameraPosition, it.second) }
  ))
}
