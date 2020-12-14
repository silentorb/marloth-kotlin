package simulation.entities

import marloth.scenery.enums.Text
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.handleCommands
import simulation.main.Deck

data class ContractDefinition(
    val name: Text,
    val tasks: List<Any> = listOf(),
    val reward: Int,
)

enum class ContractStatus {
  active,
  canceled,
  completed,
}

data class Contract(
    val client: Id,
    val agent: Id,
    val definition: ContractDefinition,
    val status: ContractStatus,
    val start: Long = -1L,
)

object ContractCommands {
  const val removeAvailableContract = "removeAvailableContract"
  const val contractCompleted = "reportContractCompleted"
  const val payAgent = "payAgent"
  const val tooSoon = "tooSoon"
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

fun getContracts(deck: Deck, client: Id, agent: Id, status: ContractStatus): Table<Contract> =
    getContracts(deck, client, agent)
        .filter { it.value.status == status }


val updateContract = handleCommands<Contract> { command, contract ->
  when (command.type) {
    ContractCommands.contractCompleted -> contract.copy(
        status = ContractStatus.completed
    )
    else -> contract
  }
}

fun updateContracts(commands: Commands, contracts: Table<Contract>): Table<Contract> =
    contracts.mapValues { (key, value) ->
      updateContract(commands.filter { it.target == key }, value)
    }
