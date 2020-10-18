package simulation.intellect.navigation

import org.recast4j.detour.NavMeshQuery
import silentorb.mythic.ent.Id
import simulation.main.Deck

fun newNavigationState(meshIds: Set<Id>, deck: Deck): NavigationState? {
  val mesh = newNavMesh(meshIds, deck)

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
