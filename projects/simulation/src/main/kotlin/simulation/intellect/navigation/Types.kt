package simulation.intellect.navigation

import org.recast4j.detour.NavMesh
import org.recast4j.detour.NavMeshQuery
import org.recast4j.detour.crowd.Crowd
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3

typealias AgentId = Int

data class NavigationState(
    val mesh: NavMesh,
    val query: NavMeshQuery,
    val crowd: Crowd,
    val agents: Map<Id, AgentId>
)

typealias NavigationDirection = Vector3
