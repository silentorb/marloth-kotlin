package simulation.intellect.assessment

import silentorb.mythic.debugging.getDebugFloat
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.BulletState
import silentorb.mythic.physics.castCollisionRay
import silentorb.mythic.scenery.Light
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.quadOut
import simulation.entities.fieldOfView360
import simulation.main.Deck
import simulation.misc.CellAttribute
import simulation.misc.MapGrid
import simulation.misc.Realm
import simulation.misc.getPointCell

const val viewingRange = 30f
const val minimumLightRating = 0.1f

fun isInAngleOfView(facingVector: Vector3, viewerBody: Body, targetBody: Body, fieldOfView: Float): Boolean =
    if (fieldOfView == fieldOfView360)
      true
    else
      facingVector.dot((targetBody.position - viewerBody.position).normalize()) >= fieldOfView

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
  return lightsMod(deck, body) + motionMod(deck, body) + (getDebugFloat("LIGHT_RATING_MOD") ?: 0f)
}

fun lightRatings(deck: Deck): Table<Float> =
    deck.characters.keys.associateWith { lightRating(deck, it) }

// Even in darkness AI will spot others if close enough
fun nearMod(distance: Float): Float =
    (1 - unitRange(3f, distance)) * 1.2f

fun areEnemies(deck: Deck, first: Id, second: Id): Boolean =
    deck.characters[first]!!.faction != deck.characters[second]!!.faction

fun isHiddenByHome(grid: MapGrid, deck: Deck, viewer: Id, target: Id): Boolean =
    areEnemies(deck, viewer, target)
        && grid.cells[getPointCell(deck.bodies[target]!!.position)]?.attributes?.contains(CellAttribute.home) ?: false

fun canSee(realm: Realm, bulletState: BulletState, deck: Deck, lightRatings: Table<Float>, viewer: Id): (Id) -> Boolean = { target ->
  val viewerBody = deck.bodies[viewer]!!
  val targetBody = deck.bodies[target]!!
  val distance = viewerBody.position.distance(targetBody.position)
  val viewerCharacterDefinition = deck.characters[viewer]!!.definition
  val fieldOfView = viewerCharacterDefinition.fieldOfView
  val result = distance <= viewingRange
      && isInAngleOfView(deck.characterRigs[viewer]!!.facingVector, viewerBody, targetBody, fieldOfView)
      && lightRatings[target]!! + nearMod(distance) >= minimumLightRating
      && !isHiddenByHome(realm.grid, deck, viewer, target)
      && castCollisionRay(bulletState.dynamicsWorld, viewerBody.position, targetBody.position) != null
  result
}

fun getVisibleCharacters(realm: Realm, bulletState: BulletState, deck: Deck, lightRatings: Table<Float>, character: Id): List<Id> {
  return deck.characters.keys
      .minus(character)
      .filter(canSee(realm, bulletState, deck, lightRatings, character))
}
