package serving

import commanding.CommandType
import haft.Commands
import simulation.MetaWorld
import simulation.World

import simulation.updateWorld

//class Server {
//  var gameSession: GameSession? = null
//
//  fun newGame(metaWorld: MetaWorld) {
//    gameSession = GameSession(World(metaWorld))
//  }
//
//  fun update(commands: Commands<CommandType>, delta: Float) {
//    if (gameSession != null) {
//      updateWorld(gameSession!!.world, commands, delta)
//    }
//  }
//}