package serving

import commanding.CommandType
import haft.Commands

import simulation.World
import simulation.updateWorld

class Server {
  val world = World()

  fun update(commands: Commands<CommandType>, delta: Float) {
    updateWorld(world, commands, delta)
  }
}