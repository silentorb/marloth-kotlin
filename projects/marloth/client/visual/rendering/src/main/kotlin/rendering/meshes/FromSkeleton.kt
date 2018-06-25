package rendering.meshes

import mythic.breeze.Bones
import mythic.breeze.getBoneTranslation
import mythic.sculpting.FlexibleMesh
import mythic.sculpting.createCylinder
import mythic.sculpting.transformFaces
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.transform
import org.joml.minus
import rendering.Material
import rendering.MeshGroup
import rendering.Model

fun rotateToward(matrix: Matrix, dir: Vector3): Matrix =
    if (dir.x == 0f && dir.y == 0f)
      matrix.rotateTowards(dir, Vector3(0f, 1f, 0f))
    else
      matrix.rotateTowards(dir, Vector3(0f, 0f, 1f))

fun modelFromSkeleton(bones: Bones, name: String): Model {
  val mesh = FlexibleMesh()

  bones.filter { it.parent != null }
      .forEach { bone ->
        val boneTransform = bone.transform(bones, bone)
        val parentTranslation = getBoneTranslation(bones, bone.parent!!)
        val boneTranslation = Vector3().transform(boneTransform)
        val length = boneTranslation.distance(parentTranslation)
        val cylinder = createCylinder(mesh, 0.03f, 8, length)
        val transform = rotateToward(boneTransform, parentTranslation - boneTranslation)
        transformFaces(transform, cylinder)
      }

  return Model(
      mesh = mesh,
      info = MeshInfo(),
      groups = listOf(
          MeshGroup(Material(Vector4(0.4f, 0.25f, 0.0f, 1f)), mesh.faces, name)
      ))
}