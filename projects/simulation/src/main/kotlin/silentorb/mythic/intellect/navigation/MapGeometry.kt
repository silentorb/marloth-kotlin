package silentorb.mythic.intellect.navigation

import org.recast4j.recast.AreaModification
import org.recast4j.recast.ConvexVolume
import org.recast4j.recast.geom.InputGeomProvider
import org.recast4j.recast.geom.TriMesh
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.scenery.getAbsoluteNodeTransform
import silentorb.mythic.ent.scenery.getLocalTransforms
import silentorb.mythic.ent.scenery.getShape
import silentorb.mythic.physics.PhysicsDeck
import silentorb.mythic.physics.getBodyTransform
import silentorb.mythic.scenery.Shape
import silentorb.mythic.shapemeshes.getShapeVertices
import simulation.main.Deck

const val SAMPLE_POLYAREA_TYPE_WALKABLE = 0x3f
val walkable = AreaModification(SAMPLE_POLYAREA_TYPE_WALKABLE)

data class GeometryProvider(
    val _meshes: MutableIterable<TriMesh>,
    val _convexVolumes: MutableIterable<ConvexVolume>,
    val _meshBoundsMin: FloatArray,
    val _meshBoundsMax: FloatArray
) : InputGeomProvider {
  override fun meshes(): MutableIterable<TriMesh> = _meshes
  override fun convexVolumes(): MutableIterable<ConvexVolume> = _convexVolumes
  override fun getMeshBoundsMin(): FloatArray = _meshBoundsMin
  override fun getMeshBoundsMax(): FloatArray = _meshBoundsMax
}

fun newNavMeshTriMeshes(meshShapeMap: Map<String, Shape>, graph: Graph, architectureElements: Collection<Key>,
                        entities: Set<Id>, deck: Deck): List<TriMesh> {
  val localTransforms = getLocalTransforms(graph)
  return architectureElements
      .map { node ->
        val shape = getShape(meshShapeMap, graph, node)!!
        val mesh = getShapeVertices(shape)
        Pair(node, mesh)
      }
      .map { (node, mesh) ->
        val transform = getAbsoluteNodeTransform(graph, localTransforms, node)
        val vertices = mesh.vertices.flatMap {
          val temp = it.transform(transform)
          // Convert to an array and Recast's Y-up coordinate system
          listOf(temp.x, temp.z, temp.y)
        }
            .toFloatArray()
        val faces = mesh.triangles.toIntArray()
        TriMesh(vertices, faces)
      } +
      entities
          .map { id ->
            val shape = deck.collisionObjects[id]!!.shape
            val mesh = getShapeVertices(shape)
            Pair(id, mesh)
          }
          .map { (id, mesh) ->
            val body = deck.bodies[id]!!
            val transform = getBodyTransform(body).scale(body.scale)
            val vertices = mesh.vertices.flatMap {
              val temp = it.transform(transform)
              // Convert to an array and Recast's Y-up coordinate system
              listOf(temp.x, temp.z, temp.y)
            }
                .toFloatArray()
            val faces = mesh.triangles.toIntArray()
            TriMesh(vertices, faces)
          }
}
