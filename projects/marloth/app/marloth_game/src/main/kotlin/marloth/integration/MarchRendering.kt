package marloth.integration

import mythic.ent.Id
import mythic.spatial.Vector3
import mythic.spatial.getCenter
import silentorb.raymarching.Sdf
import silentorb.raymarching.plus
import silentorb.raymarching.sphereSdf
import simulation.World

fun prepareSceneSdf(player: Id, world: World): Sdf {
  val deck = world.deck
  val playerNode = deck.bodies[player]!!.node
  val walls = world.realm.nodeTable[playerNode]!!.walls
      .map { face ->
        val wall = world.realm.mesh.faces[face]!!
        sphereSdf(getCenter(wall.vertices), 10f)
      }
      .plus(sphereSdf(Vector3(-58.23136f, 20.66085f, 1.4f), 10f))

  val centers=world.realm.nodeTable[playerNode]!!.walls
      .map { face ->
        val wall = world.realm.mesh.faces[face]!!
        getCenter(wall.vertices)
      }

//  return plus(walls)
  return sphereSdf(Vector3(-58.23136f, 20.66085f, 1.4f), 10f)
}