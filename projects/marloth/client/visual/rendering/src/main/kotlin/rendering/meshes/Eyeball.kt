package rendering.meshes

import mythic.sculpting.ImmutableMesh
import mythic.sculpting.calculateNormals
import mythic.sculpting.transformMesh
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Vector4
import mythic.spatial.getCenter
import rendering.Material
import rendering.MeshGroup
import rendering.Model
import rendering.mapMaterialToManyMeshes

fun createTransformedSphere(radius: Float, transform: Matrix): ImmutableMesh {
  val mesh = ImmutableMesh()
  val res = 16
  throw Error("No longer used")
//  mythic.sculpting.createSphere(mesh, radius, res, res)
//  transformMesh(mesh, transform)
//  return mesh
}

val createEyeball: ModelGenerator = {
  val radius = 0.5f
  val floorOffset = 0.5f
  val mesh = createTransformedSphere(radius, Matrix()
      .translate(0f, 0f, radius + floorOffset)
      .rotateX(Pi / 2f)
  )
  val (pupilIris, ball) = mesh.faces.partition { getCenter(it.vertices).y < -0.38f }
  val (pupil, iris) = pupilIris.partition { getCenter(it.vertices).y < -0.48f }

  val whiteMaterial = Material(Vector4(0.9f, 0.9f, 0.9f, 1f))
  val irisMaterial = Material(Vector4(0.8f, 0.4f, 0.2f, 1f))
  val pupilMaterial = Material(Vector4(0f, 0f, 0f, 1f))

  val materialMap = listOf(
      MeshGroup(whiteMaterial, ball),
      MeshGroup(irisMaterial, iris),
      MeshGroup(pupilMaterial, pupil)
  )

  calculateNormals(mesh)

  Model(
      mesh = mesh,
      groups = materialMap
  )
}