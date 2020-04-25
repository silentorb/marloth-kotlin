package marloth.clienting.hud

import silentorb.mythic.characters.targeting.*
import silentorb.mythic.haft.HaftCommands
import simulation.main.World

fun getGetAvailableTargets(world: World): GetAvailableTargets = { actor ->
  val deck = world.deck
  deck.characters
      .minus(actor)
      .filterValues { it.isAlive }
      .keys
      .toList()
}

fun autoSelectTarget(world: World): AutoSelectTarget = { actor, options ->
  val deck = world.deck
  options.first()
}

fun updateTargeting(world: World, commands: HaftCommands, targets: TargetTable): TargetTable {
  val toggleEvents = commands.filter { it.type == toggleTargetingCommand }.map { it.target }.toSet()
  return updateTargeting(targets, toggleEvents, getGetAvailableTargets(world), autoSelectTarget(world))
}
