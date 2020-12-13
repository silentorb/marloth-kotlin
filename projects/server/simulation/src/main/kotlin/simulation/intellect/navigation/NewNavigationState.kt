package simulation.intellect.navigation

import org.recast4j.detour.NavMeshQuery
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Key
import silentorb.mythic.scenery.Shape
import simulation.main.Deck

fun newNavigationState(meshShapeMap: Map<String, Shape>, meshIds: Collection<Key>, graph: Graph): NavigationState? {
  val mesh = newNavMesh(meshShapeMap, meshIds, graph)

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
