package junk_client.views

import junk_simulation.*
import mythic.bloom.*
import mythic.spatial.Vector2

data class ClientBattleState(
    val placeholder: Int = 1
)

val elementColors = mapOf(
    Element.ethereal to lightBlue,
    Element.plant to lightGreen,
    Element.robot to lightRed
)

fun resourceView(resource: Resource, bounds: Bounds) =
    label(
        elementColors[resource.element]!!,
        "" + resource.value + "/" + resource.max,
        bounds
    )

val abilityHeight = itemHeight * 2f

fun abilityView(ability: Ability, bounds: Bounds): List<Box> {
  val rows = arrangeVertical(standardPadding, bounds, listOf(itemHeight, null))
  return listOf(
      label(white, ability.type.name + " " + ability.level, rows[0]),
      label(white, (0 until ability.cooldown).map { "* " }.joinToString(), rows[1])
  )
}

fun playerView(player: Character, state: ClientBattleState, bounds: Bounds): Layout {
  val rows = arrangeVertical(standardPadding, bounds, listOf(40f, null))
  val resourceBoxes = verticalList(player.resources, rows[0], itemHeight, { r, b -> listOf(resourceView(r, b)) })
  val abilityBoxes = verticalList(player.abilities, rows[1], abilityHeight, { a, b -> abilityView(a, b) })
  return resourceBoxes.plus(abilityBoxes)
}

fun battleView(state: ClientBattleState, world: World, bounds: Bounds): Layout {
//  val columnLengths = resolveLengths(bounds.dimensions.x, listOf(100f, null, 100f))
//  val columns = listBounds(horizontalPlane, Vector2(), bounds, columnLengths)
  val columns = arrangeHorizontal(standardPadding, bounds, listOf(120f, null, 120f))
  return listOf<Box>()
      .plus(playerView(world.player, state, columns[2]))
}
