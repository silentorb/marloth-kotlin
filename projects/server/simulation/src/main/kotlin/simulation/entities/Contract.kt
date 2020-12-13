package simulation.entities

import marloth.scenery.enums.Text
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Commands
import simulation.main.Deck

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

object ContractCommands {
  const val removeAvailableContract = "removeAvailableContract"
  const val reportContractCompleted = "reportContractCompleted"
}

data class ClearAreaTask(
    val zone: Key,
)

fun updateAvailableContracts(commands: Commands, availableContracts: Map<Id, ContractDefinition>): Map<Id, ContractDefinition> =
    availableContracts - commands
        .filter { it.type == ContractCommands.removeAvailableContract }
        .map { it.value as Id }

fun getContracts(deck: Deck, client: Id, agent: Id): Table<Contract> =
    deck.contracts.filterValues { contract ->
      contract.agent == agent && contract.client == client
    }
