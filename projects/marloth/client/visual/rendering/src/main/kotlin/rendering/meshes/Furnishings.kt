package rendering.meshes

import mythic.sculpting.FlexibleMesh
import mythic.sculpting.createCylinder
import mythic.sculpting.transformMesh
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Vector4
import rendering.Material
import rendering.Model
import rendering.ModelGenerator
import rendering.mapMaterial


fun createTransformedCylinder(length: Float, radius: Float, transform: Matrix): FlexibleMesh {
  val mesh = FlexibleMesh()
  createCylinder(mesh, radius, 8, length)
  transformMesh(mesh, transform)
  return mesh
}

val createWallLamp: ModelGenerator = {
  val length = 0.75f
  val radius = 0.1f
  val secondLength = 0.4f
  val firstGlassLength = 0.55f
  val first = createTransformedCylinder(length, radius, Matrix().rotateY(Pi / 2))
  val second = createTransformedCylinder(secondLength, radius, Matrix().translate(length - radius, 0f, 0f))
  val glass1 = createTransformedCylinder(firstGlassLength, 0.3f, Matrix().translate(length - radius, 0f, secondLength))
  val glass2 = createTransformedCylinder(0.65f, 0.15f, Matrix().translate(length - radius, 0f, secondLength + firstGlassLength))

  val brassMaterial = Material(Vector4(0.3f, 0.25f, 0.1f, 1f))
  val glassMaterial = Material(Vector4(0.9f, 0.9f, 0.6f, 0.5f), glow = 0.6f)

  val materialMap = listOf(
      mapMaterial(brassMaterial, listOf(first, second)),
      mapMaterial(glassMaterial, listOf(glass1, glass2))
  )

  first.sharedImport(listOf(
      second,
      glass1,
      glass2
  ))

  Model(
      mesh = first,
      materials = materialMap)
}