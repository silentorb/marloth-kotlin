package silentorb.mythic.intellect.navigation

import org.recast4j.detour.NavMesh
import org.recast4j.detour.NavMeshQuery

data class NavigationState(
    val navMesh: NavMesh,
    val navMeshQuery: NavMeshQuery
)
