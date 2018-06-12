package junk_client.views

import junk_client.EntitySelectionEvent
import junk_simulation.*
import junk_simulation.logic.isReady
import mythic.bloom.*
import mythic.drawing.Canvas

data class ClientBattleState(
    val selectedEntity: Id?
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

fun abilityEvent(creature: Creature, ability: Ability): EntitySelectionEvent? =
    if (isReady(creature, ability))
      EntitySelectionEvent(ability.id)
    else
      null

fun selectedDepiction(state: ClientBattleState, id: Id): Depiction? =
    if (state.selectedEntity == id)
      { bounds: Bounds, canvas: Canvas ->
        drawBorder(bounds, canvas, LineStyle(red, 1f))
      }
    else
      null

fun creatureView(state: ClientBattleState, creature: Creature, bounds: Bounds): Layout {
  val columns = arrangeHorizontal(standardPadding, bounds, listOf(80f, null))
  val resources = verticalList(creature.resources, columns[1], itemHeight, 0f, { a, b -> listOf(resourceView(a, b)) })
  return listOf<Box>()
      .plus(label(white, creature.type.name.take(8), columns[0]))
      .plus(resources)
}

fun creaturesView(world: World, state: ClientBattleState, bounds: Bounds): Layout {
  return verticalList(world.enemies, bounds, 20f, 0f, { a, b -> creatureView(state, a, b) })
}

fun abilityView(state: ClientBattleState, creature: Creature, ability: Ability, bounds: Bounds): List<Box> {
  val rows = arrangeVertical(standardPadding, bounds, listOf(itemHeight, null))
  return listOf(
      label(white, ability.type.name, rows[0]),
      label(white, (0 until ability.cooldown).map { "* " }.joinToString(), rows[1]),
      Box(
          bounds = bounds,
          depiction = selectedDepiction(state, ability.id),
          handler = abilityEvent(creature, ability)
      )
  )
}

fun playerView(player: Creature, state: ClientBattleState, bounds: Bounds): Layout {
  val rows = arrangeVertical(standardPadding, bounds, listOf(40f, null))
  val resourceBoxes = verticalList(player.resources, rows[0], itemHeight, 0f, { r, b -> listOf(resourceView(r, b)) })
  val abilityBoxes = verticalList(player.abilities, rows[1], abilityHeight, 0f, { a, b -> abilityView(state, player, a, b) })
  return resourceBoxes.plus(abilityBoxes)
}

fun battleLayoutOutline(bounds: Bounds): Layout {
  val border = { b: Bounds ->
    Box(
        bounds = b,
        depiction = { b2: Bounds, c: Canvas ->
          drawBorder(b2, c, LineStyle(white, 1f))
        }
    )
  }

  val columnSize = 320f / 3f
  val columns = arrangeHorizontal(0f, bounds, listOf(columnSize, columnSize, columnSize))

  val creatureCount = 10
  val creatureHeight = 200f / creatureCount
  val creaturesOutline = { bounds2: Bounds ->
    verticalList((1..creatureCount).toList(), bounds2, creatureHeight, 0f, { a, b -> listOf(border(b)) })
  }
  return columns.flatMap { creaturesOutline(it) }
}

fun battleView(state: ClientBattleState, world: World, bounds: Bounds): Layout {
  val columns = arrangeHorizontal(standardPadding, bounds, listOf(120f, null, 120f))
  return battleLayoutOutline(bounds)
      .plus(creaturesView(world, state, columns[0]))
      .plus(playerView(world.player, state, columns[2]))
}
