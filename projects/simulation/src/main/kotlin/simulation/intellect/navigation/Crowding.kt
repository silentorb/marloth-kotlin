package simulation.intellect.navigation

import org.recast4j.detour.NavMesh
import org.recast4j.detour.crowd.Crowd

fun newCrowd(mesh: NavMesh): Crowd {
  val crowd = Crowd(100, agentRadius, mesh)
  return crowd
}
