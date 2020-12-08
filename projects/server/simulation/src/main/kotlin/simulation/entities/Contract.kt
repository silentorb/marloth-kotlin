package simulation.entities

import marloth.scenery.enums.Text
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Key
import silentorb.mythic.happenings.Commands

data class ContractDefinition(
    val name: Text,
    val tasks: List<Any> = listOf(),
    val reward: Int,
)

enum class ContractStatus {
  active,
  completed,
  failed,
}

data class Contract(
    val client: Id,
    val agent: Id,
    val definition: ContractDefinition,
    val status: ContractStatus
)

const val removeAvailableContractCommand = "removeAvailableContract"

data class ClearAreaTask(
    val zone: Key,
)

fun updateAvailableContracts(commands: Commands, availableContracts: Map<Id, ContractDefinition>): Map<Id, ContractDefinition> =
    availableContracts - commands
        .filter { it.type == removeAvailableContractCommand }
        .map { it.value as Id }
