package rendering.meshes

import mythic.breeze.*
import mythic.sculpting.*
import mythic.spatial.*
import org.joml.minus
import org.joml.times
import rendering.*
import rendering.VertexWeight
import rendering.VertexWeights
import rendering.WeightMap

fun modelFromSkeleton(bones: Bones, name: String): Pair<Model, WeightMap> {
  val mesh = FlexibleMesh()

  val weights = mutableMapOf<Vector3, VertexWeights>()

  bones
      .filter { it.parent != null }
      .filter { it.length != 0f }
//      .take(7)
//      .drop(6).take(1)
      .forEach{ bone ->
//        if (bone.name == "head") {
          val head = getSimpleBoneTransform(bone)
          val cylinder = createCylinder(mesh, 0.03f, 8, bone.length)
          val vertices = distinctVertices(cylinder.flatMap { it.vertices })
          for (vertex in vertices) {
            weights[vertex] = VertexWeights(
                VertexWeight(bone.index, 1f),
                VertexWeight(0, 0f)
            )
          }
//        transformVertices(Matrix().rotateY(Pi / 2f), vertices)
//        transformVertices(head, vertices)
          transformVertices(head * Matrix().rotateY(Pi / 2f), vertices)
//        }
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