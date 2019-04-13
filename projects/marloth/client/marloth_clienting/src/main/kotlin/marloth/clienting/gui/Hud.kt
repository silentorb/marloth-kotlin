package marloth.clienting.gui

import mythic.bloom.*
import org.joml.Vector2i
import simulation.Interactable
import simulation.Resource

private val textStyle = mythic.typography.IndexedTextStyle(0, 12f, mythic.drawing.grayTone(0.7f))

data class HudData(
    val health: Resource,
    val sanity: Resource,
    val interactable: Interactable?
)

fun resourceString(resource: Resource): String {
  val value = resource.value
  val max = resource.max
  return "$value / $max"
}

val df = java.text.DecimalFormat("#0.00")

//fun characterVisibility(data: HudData, id: Id): String {
//  val rating = intellect.acessment.lightRating(world, id)
//  return "vis: " + df.format(rating)
//}

fun interactionDialog(interactable: Interactable): Flower {
  val rows = listOf(
      label(textStyle, "a"),
      label(textStyle, "bee")
  )
  return centeredHorizontal(
      list(vertical, 10)(rows)
  )
}

fun hudLayout(data: HudData): Flower {
  val rows = listOf(
      label(textStyle, "HP: ${resourceString(data.health)}"),
      label(textStyle, "SAN: ${resourceString(data.sanity)}")
//      mythic.bloom.label(textStyle, characterVisibility(data, player)),
//      mythic.bloom.label(textStyle, "vel: " + df.format(world.deck.bodies[player]!!.velocity.length()))
  )

  return addFlowers(listOfNotNull(
      offset(Vector2i(10))(
          list(vertical, 10)(rows)
      ),
      if (data.interactable != null) interactionDialog(data.interactable) else null
  ))
}
