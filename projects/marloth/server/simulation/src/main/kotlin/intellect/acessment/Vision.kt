package intellect.acessment

import colliding.rayCanHitPoint
import mythic.ent.Id
import physics.Body
import simulation.Character
import simulation.DepictionType
import simulation.Light
import simulation.World

const val viewingRange = 30f
const val minimumLightRating = 0.1f

fun isInAngleOfView(viewer: Character, viewerBody: Body, targetBody: Body): Boolean =
    viewer.facingVector.dot((targetBody.position - viewerBody.position).normalize()) > 0.5f

fun isInVisibleRange(viewerBody: Body, targetBody: Body): Boolean =
    viewerBody.position.distance(targetBody.position) <= viewingRange

fun lightDistanceMod(world: World, body: Body, light: Light): Float {
  val lightBody = world.bodyTable[light.id]!!
  val distance = body.position.distance(lightBody.position)
  val distanceMod = 1f - Math.min(1f, distance / light.range)
  return distanceMod
}

fun lightRating(world: World, id: Id): Float {
  val body = world.bodyTable[id]!!
  val lightMod = Math.min(1f, world.deck.lights.map { light -> lightDistanceMod(world, body, light) }.sum())
  return lightMod
}

fun canSee(world: World, viewer: Character, target: Id): Boolean {
  val viewerBody = world.bodyTable[viewer.id]!!
  val targetBody = world.bodyTable[target]!!
  val nodes = world.realm.nodeTable
  return isInVisibleRange(viewerBody, targetBody)
      && isInAngleOfView(viewer, viewerBody, targetBody)
      && rayCanHitPoint(world.realm, nodes[viewerBody.node]!!, viewerBody.position, targetBody.position)
      && lightRating(world, target) >= minimumLightRating
}

fun getVisibleCharacters(world: World, character: Character): List<Character> {
  return world.characters.filter { it.id != character.id && canSee(world, character, it.id) }
}
