package marloth.clienting.gui

import mythic.bloom.Flower
import mythic.bloom.list
import mythic.bloom.vertical
import mythic.ent.Id
import org.joml.Vector2i
import simulation.Resource

private val textStyle = mythic.typography.IndexedTextStyle(0, 12f, mythic.drawing.grayTone(0.7f))

fun resourceString(resource: Resource): String {
  val value = resource.value
  val max = resource.max
  return "$value / $max"
}

fun characterHealth(world: simulation.World, id: Id): String {
  val resource = world.deck.characters[id]!!.health
  return "HP: ${resourceString(resource)}"
}

fun characterSanity(world: simulation.World, id: Id): String {
  val resource = world.deck.characters[id]!!.sanity
  return "SAN: ${resourceString(resource)}"
}

val df = java.text.DecimalFormat("#0.00")

fun characterVisibility(world: simulation.World, id: Id): String {
  val rating = intellect.acessment.lightRating(world, id)
  return "vis: " + df.format(rating)
}

fun hudLayout(world: simulation.World): Flower {
  val player = world.players.first().id
  val rows = listOf(
      mythic.bloom.label(textStyle, characterHealth(world, player)),
      mythic.bloom.label(textStyle, characterSanity(world, player)),
      mythic.bloom.label(textStyle, characterVisibility(world, player)),
      mythic.bloom.label(textStyle, "vel: " + df.format(world.deck.bodies[player]!!.velocity.length()))
  )
  return (mythic.bloom.offset(Vector2i(10)))(
      list(vertical, 10)(rows)
  )
}
