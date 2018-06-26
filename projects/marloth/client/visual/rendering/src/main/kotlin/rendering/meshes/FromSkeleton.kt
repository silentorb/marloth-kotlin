package rendering.meshes

import mythic.breeze.Bones
import mythic.breeze.getBoneTranslation
import mythic.sculpting.*
import mythic.spatial.*
import org.joml.minus
import rendering.*

fun modelFromSkeleton(bones: Bones, name: String): Pair<Model, WeightMap> {
  val mesh = FlexibleMesh()

  val weights = mutableMapOf<Vector3, VertexWeights>()

  bones.filter { it.parent != null }
      .drop(4).take(1)
      .forEachIndexed { index, bone ->
        val boneTransform = bone.transform(bones, bone)
        val parentTranslation = getBoneTranslation(bones, bone.parent!!)
        val boneTranslation = Vector3().transform(boneTransform)
        val length = boneTranslation.distance(parentTranslation)
        assert(length > 0f)
        val cylinder = createCylinder(mesh, 0.03f, 8, length)
        val transform = rotateToward(boneTransform, parentTranslation - boneTranslation)
        val vertices = distinctVertices(cylinder.flatMap { it.vertices })
        for (vertex in vertices) {
          weights[vertex] = VertexWeights(
              VertexWeight(index, 1f),
              VertexWeight(0, 0f)
          )
        }
        transformVertices(transform, vertices)
      }

  val model = Model(
      mesh = mesh,
      info = MeshInfo(),
      groups = listOf(
          MeshGroup(Material(Vector4(0.4f, 0.25f, 0.0f, 1f)), mesh.faces, name)
      )
  )

  return Pair(model, weights.toMap())
}