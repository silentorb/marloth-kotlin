package marloth.clienting.gui

import mythic.bloom.*
import mythic.bloom.next.*

import org.joml.Vector2i
import silentorb.mythic.debugging.getDebugRangeValue
import simulation.entities.Interactable
import simulation.entities.Modifier
import simulation.misc.ResourceContainer

private val textStyle = textStyles.gray

data class HudData(
    val health: ResourceContainer,
    val sanity: ResourceContainer,
    val interactable: Interactable?,
    val buffs: List<Pair<Modifier, Int>>,
    val debugInfo: List<String> = listOf()
)

fun resourceString(resource: ResourceContainer): String {
  val value = resource.value
  val max = resource.max
  return "$value / $max"
}

val df = java.text.DecimalFormat("#0.00")

//fun characterVisibility(data: HudData, id: Id): String {
//  val rating = simulation.intellect.acessment.lightRating(world, id)
//  return "vis: " + df.format(rating)
//}

private fun playerStats(data: HudData): Flower {
  val rows = listOf(
      label(textStyle, "Health: ${resourceString(data.health)}"),
      label(textStyle, "Sanity: ${resourceString(data.sanity)}"),
      label(textStyle, "Debug Range: ${getDebugRangeValue()}")
//      mythic.bloom.localizedLabel(textStyle, characterVisibility(data, player)),
//      mythic.bloom.localizedLabel(textStyle, "vel: " + df.format(world.deck.bodies[player]!!.velocity.length()))
  )
      .plus(data.buffs.map {
        label(textStyle, "${it.first.type.name} ${it.second}")
      })
      .plus(data.debugInfo.map {
        label(textStyle, it)
      })
  return div(forward = fixedOffset(Vector2i(10)))(
      div(reverse = shrink, depiction = solidBackground(black))(
          margin(20)(
              list(verticalPlane, 10)(rows)
          )
      )
  )
}

private val interactionBar =
    div("a",
        forward = forwardOffset(top = percentage(0.8f)),
        reverse = reverseOffset(left = centered) + reverseDimensions(height = shrinkWrap),
        depiction = solidBackground(black)
    )

fun interactionDialog(textResources: TextResources, interactable: Interactable): Flower {
  val secondary = interactable.secondaryCommand
  val rows = listOfNotNull(
      label(textStyle, textResources[interactable.primaryCommand.text]!! + " A"),
      if (secondary != null)
        label(textStyle, textResources[secondary.text]!! + " B")
      else
        null
  )

  val gap = 20
  return interactionBar(
      div("b", reverse = reverseOffset(left = centered) + reverseDimensions(height = shrinkWrap))(
          margin(top = gap, bottom = gap)(
              list(verticalPlane, gap)(rows)
          )
      )
  )
}

fun hudLayout(textResources: TextResources, data: HudData): Flower {
  return compose(listOfNotNull(
      playerStats(data),
      if (data.interactable != null) interactionDialog(textResources, data.interactable) else null
  ))
}
