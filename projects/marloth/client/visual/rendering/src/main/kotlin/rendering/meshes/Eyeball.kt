package rendering.meshes

import mythic.sculpting.FlexibleMesh
import mythic.sculpting.transformMesh
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Vector4
import rendering.Material
import rendering.Model
import rendering.mapMaterialToManyMeshes

fun createTransformedSphere(radius: Float, transform: Matrix): FlexibleMesh {
  val mesh = FlexibleMesh()
  mythic.sculpting.createSphere(mesh, radius, 8, 8)
  transformMesh(mesh, transform)
  return mesh
}

val createEyeball: ModelGenerator = {
  val radius = 0.5f
  val floorOffset = 0.5f
  val ball = createTransformedSphere(radius, Matrix()
      .translate(0f, 0f, radius + floorOffset)
  )
  val pupil = createTransformedSphere(0.3f, Matrix()
      .translate(0f, -0.3f, radius + floorOffset)
  )

  val whiteMaterial = Material(Vector4(0.9f, 0.9f, 0.9f, 1f))
  val pupilMaterial = Material(Vector4(0f, 0f, 0f, 1f))

  val materialMap = listOf(
      mapMaterialToManyMeshes(whiteMaterial, listOf(ball)),
      mapMaterialToManyMeshes(pupilMaterial, listOf(pupil))
  )

  ball.sharedImport(listOf(
      pupil
  ))

  Model(
      mesh = ball,
      groups = materialMap
  )
}