package simulation.intellect.assessment

import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.quadOut
import simulation.entities.Light
import simulation.main.Deck
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.BulletState
import silentorb.mythic.physics.castCollisionRay
import silentorb.mythic.characters.CharacterRig

const val viewingRange = 30f
const val minimumLightRating = 0.0f

fun isInAngleOfView(viewer: CharacterRig, viewerBody: Body, targetBody: Body): Boolean =
    viewer.facingVector.dot((targetBody.position - viewerBody.position).normalize()) > 0.5f

fun lightDistanceMod(deck: Deck, body: Body, id: Id, light: Light): Float {
  val lightBody = deck.bodies[id]!!
  val distance = body.position.distance(lightBody.position)
  val distanceMod = 1f - Math.min(1f, distance / light.range)
  return quadOut(distanceMod)
}

private fun lightsMod(deck: Deck, body: Body): Float =
    Math.min(1f, deck.lights.map { (id, light) -> lightDistanceMod(deck, body, id, light) }.sum())

private const val maxVelocityMod = 10f
private const val velocityModStrength = 0.4f

fun unitRange(max: Float, value: Float): Float =
    Math.min(max, value) / max

private fun motionMod(deck: Deck, body: Body): Float =
    unitRange(maxVelocityMod, body.velocity.length()) * velocityModStrength

fun lightRating(deck: Deck, id: Id): Float {
  val body = deck.bodies[id]!!
  return lightsMod(deck, body) + motionMod(deck, body)
}

// Even in darkness AI will spot others if close enough
fun nearMod(distance: Float): Float =
    (1 - unitRange(3f, distance)) * 1.2f

fun canSee(bulletState: BulletState, deck: Deck, viewer: Id): (Id) -> Boolean = { target ->
  val viewerBody = deck.bodies[viewer]!!
  val targetBody = deck.bodies[target]!!
  val distance = viewerBody.position.distance(targetBody.position)
  distance <= viewingRange
      && isInAngleOfView(deck.characterRigs[viewer]!!, viewerBody, targetBody)
      && castCollisionRay(bulletState.dynamicsWorld, viewerBody.position, targetBody.position) != null
      && lightRating(deck, target) + nearMod(distance) >= minimumLightRating
}

fun getVisibleCharacters(bulletState: BulletState, deck: Deck, character: Id): List<Id> {
  return deck.characters.keys
      .minus(character)
      .filter(canSee(bulletState, deck, character))
}
