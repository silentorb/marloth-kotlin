package rendering.meshes

import mythic.sculpting.FlexibleMesh
import mythic.sculpting.createCube
import mythic.sculpting.createSphere
import mythic.sculpting.translateMesh
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import rendering.Material
import rendering.Model
import rendering.mapMaterialToMesh

val createCube = {
  val mesh = FlexibleMesh()
//  create.squareDown(mesh, Vector2(1f, 1f), 1f)
  createCube(mesh, Vector3(1f, 1f, 1f))
  Model(
      mesh = mesh,
      materials = listOf(mapMaterialToMesh(Material(Vector4(.5f, .5f, .5f, 1f)), mesh))
  )
}

val createSphere = {
  val mesh = FlexibleMesh()
  createSphere(mesh, 0.3f, 8, 6)
  translateMesh(mesh, Vector3(0f, 0f, 1f))
  Model(
      mesh = mesh,
      materials = listOf(mapMaterialToMesh(Material(Vector4(0.4f, 0.1f, 0.1f, 1f)), mesh))
  )
}

fun createCylinder(): FlexibleMesh {
  val mesh = FlexibleMesh()
  mythic.sculpting.createCylinder(mesh, 0.5f, 8, 1f)
  return mesh
}
