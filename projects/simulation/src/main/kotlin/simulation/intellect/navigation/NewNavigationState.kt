package simulation.intellect.navigation

import org.recast4j.detour.NavMeshQuery
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.Graph
import silentorb.mythic.intellect.navigation.newNavMeshTriMeshes
import silentorb.mythic.scenery.Shape
import simulation.main.Deck

fun newNavigationState(meshShapeMap: Map<String, Shape>,
                       nodes: Collection<Key>, graph: Graph,
                       entities: Set<Id>, deck: Deck): NavigationState? {
  val meshes = newNavMeshTriMeshes(meshShapeMap, graph, nodes, entities, deck)
  val mesh = newNavMesh(meshes)

  return if (mesh == null)
    null
  else
    NavigationState(
        mesh = mesh,
        query = NavMeshQuery(mesh),
        crowd = newCrowd(mesh),
        agents = mapOf()
    )
}
