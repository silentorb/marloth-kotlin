package intellect.acessment

import colliding.rayCanHitPoint
import mythic.ent.Id
import mythic.spatial.quadOut
import physics.Body
import simulation.Character
import simulation.Light
import simulation.World

const val viewingRange = 30f
const val minimumLightRating = 0.2f

fun isInAngleOfView(viewer: Character, viewerBody: Body, targetBody: Body): Boolean =
    viewer.facingVector.dot((targetBody.position - viewerBody.position).normalize()) > 0.5f

fun lightDistanceMod(world: World, body: Body, light: Light): Float {
  val lightBody = world.bodyTable[light.id]!!
  val distance = body.position.distance(lightBody.position)
  val distanceMod = 1f - Math.min(1f, distance / light.range)
  return quadOut(distanceMod)
}

private fun lightsMod(world: World, body: Body): Float =
    Math.min(1f, world.deck.lights.values.map { light -> lightDistanceMod(world, body, light) }.sum())

private const val maxVelocityMod = 10f
private const val velocityModStrength = 0.4f

fun unitRange(max: Float, value: Float): Float =
    Math.min(max, value) / max

private fun motionMod(world: World, body: Body): Float =
    unitRange(maxVelocityMod, body.velocity.length()) * velocityModStrength

fun lightRating(world: World, id: Id): Float {
  val body = world.bodyTable[id]!!
  return lightsMod(world, body) + motionMod(world, body)
}

// Even in darkness AI will spot others if close enough
fun nearMod(distance: Float): Float =
    (1 - unitRange(4f, distance)) * 1.2f

fun canSee(world: World, viewer: Character, target: Id): Boolean {
  val viewerBody = world.bodyTable[viewer.id]!!
  val targetBody = world.bodyTable[target]!!
  val nodes = world.realm.nodeTable
  val distance = viewerBody.position.distance(targetBody.position)
  return distance <= viewingRange
      && isInAngleOfView(viewer, viewerBody, targetBody)
      && rayCanHitPoint(world.realm, nodes[viewerBody.node]!!, viewerBody.position, targetBody.position)
      && lightRating(world, target) + nearMod(distance) >= minimumLightRating
}

fun getVisibleCharacters(world: World, character: Character): List<Character> {
  return world.characters.filter { it.id != character.id && it.isAlive && canSee(world, character, it.id) }
}
