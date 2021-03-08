package simulation.intellect.execution

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import simulation.entities.ContractCommands
import simulation.happenings.Request
import simulation.happenings.notify
import simulation.happenings.requestCommandType
import simulation.intellect.Spirit
import simulation.main.World
import simulation.updating.simulationFps

fun spiritHandleRequest(world: World, spirit: Id, request: Request): Commands =
    when (request.type) {
      ContractCommands.contractCompleted -> {
        val contract = request.value as Id
        val contractRecord = world.deck.contracts[contract]
        if (contractRecord != null) {
          val duration = (world.step - contractRecord.start) / simulationFps
          if (duration < 30)
            listOf(
                notify(contractRecord.agent, "You couldn't be done yet."),
            )
          else
            listOf(
                Command(ContractCommands.contractCompleted, target = contract),
                Command(ContractCommands.payAgent, target = contractRecord.agent, value = contractRecord.definition.reward),
            )
        } else
          listOf()
      }

      else -> listOf()
    }

fun spiritsHandleRequests(world: World, spirits: Table<Spirit>, commands: Commands): Commands {
  val requestCommands = commands.filter { it.type == requestCommandType }
  return if (requestCommands.none())
    listOf()
  else
    spirits
        .flatMap { (id, _) ->
          requestCommands
              .filter { it.target == id && it.value is Request }
              .flatMap { spiritHandleRequest(world, id, it.value as Request) }
        }
}
