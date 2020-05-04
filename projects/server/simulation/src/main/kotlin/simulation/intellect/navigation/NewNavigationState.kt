package simulation.intellect.navigation

import org.recast4j.detour.NavMeshQuery
import silentorb.mythic.ent.Id
import silentorb.mythic.intellect.navigation.NavigationState
import simulation.main.Deck

fun newNavigationState(meshIds: Set<Id>, deck: Deck): NavigationState {
  val mesh = newNavMesh(meshIds, deck)

  return NavigationState(
      mesh = mesh,
      query = NavMeshQuery(mesh)
  )
}
