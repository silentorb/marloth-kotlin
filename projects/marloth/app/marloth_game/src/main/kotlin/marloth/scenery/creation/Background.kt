package marloth.scenery.creation

import marloth.integration.BackgroundTextureId
import mythic.ent.Table
import mythic.spatial.*
import rendering.ElementGroup
import rendering.ElementGroups
import rendering.Material
import rendering.MeshElement
import marloth.scenery.enums.MeshId
import simulation.entities.Cycle

fun createBackgroundSphere(texture: BackgroundTextureId, cameraPosition: Vector3, orientation: Quaternion = Quaternion()) =
    MeshElement(
        id = 1,
        mesh = MeshId.skySphere.toString(),
        transform = Matrix()
            .translate(cameraPosition)
            .rotate(orientation)
            .scale(100f),
        material = Material(
            color = Vector4(1f, 1f, 1f, 1f),
            texture = texture.name,
            shading = false
        )
    )

fun gatherBackground(cycles: Table<Cycle>, cameraPosition: Vector3): ElementGroups {
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
