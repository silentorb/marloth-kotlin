package marloth.clienting.gui

import mythic.bloom.Flower
import mythic.bloom.list
import mythic.bloom.vertical
import mythic.ent.Id
import org.joml.Vector2i

private val textStyle = mythic.typography.IndexedTextStyle(0, 12f, mythic.drawing.grayTone(0.7f))

fun characterHealth(world: simulation.World, id: Id): String {
  val resource = world.deck.characters[id]!!.health
  val value = resource.value
  val max = resource.max
  return "$value / $max"
}

val df = java.text.DecimalFormat("#0.00")

fun characterVisibility(world: simulation.World, id: Id): String {
  val rating = intellect.acessment.lightRating(world, id)
  return df.format(rating)
}

fun hudLayout(world: simulation.World): Flower {
  val player = world.players.first().id
  val rows = listOf(
      mythic.bloom.label(textStyle, characterHealth(world, player)),
      mythic.bloom.label(textStyle, characterVisibility(world, player)),
      mythic.bloom.label(textStyle, df.format(world.deck.bodies[player]!!.velocity.length()))
  )
  return (mythic.bloom.offset(Vector2i(10)))(
      list(vertical, 10)(rows)
  )
}
