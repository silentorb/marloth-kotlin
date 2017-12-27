package serving

import commanding.Commands
import simulation.World
import simulation.updateWorld

class Server {
  val world = World()

  fun update(commands: Commands) {
    updateWorld(world, commands)
  }
}