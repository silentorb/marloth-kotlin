package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Key

data class ContractDefinition(
    val tasks: List<Any> = listOf(),
)

data class Contract(
    val client: Id,
    val agent: Id,
    val tasks: List<Any> = listOf(),
)

data class ClearAreaTask(
    val zone: Key,
)
