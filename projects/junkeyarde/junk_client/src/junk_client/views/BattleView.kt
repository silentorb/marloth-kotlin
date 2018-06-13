package junk_client.views

import junk_client.EntitySelectionEvent
import junk_simulation.*
import junk_simulation.logic.isReady
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone

val columnCount = 4
val rowCount = 8
val columnSize = 320f / columnCount
val rowSize = 200f / rowCount

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
//  val resources = verticalList(creature.resources, columns[1], itemHeight, 0f, { a, b -> listOf(resourceView(a, b)) })
  return listOf<Box>()
      .plus(label(white, creature.type.name.take(8), columns[0]))
      .plus(label(white, creature.life.toString(), columns[1]))
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

fun playerView(creature: Creature, state: ClientBattleState, bounds: Bounds): Layout {
  val rows = arrangeVertical(0f, bounds, (1..rowCount).toList().map { rowSize })
  val abilityBoxes = creature.abilities.zip(rows.drop(1), { a, b ->
    abilityView(state, creature, a, b)
  }).flatten()
  return listOf(label(white, creature.life.toString(), rows[0])).plus(abilityBoxes)
}

fun battleLayoutOutline(bounds: Bounds): Layout {
  val border = { b: Bounds ->
    Box(
        bounds = b,
        depiction = { b2: Bounds, c: Canvas ->
          drawBorder(b2, c, LineStyle(grayTone(0.3f), 1f))
        }
    )
  }

  val k = (1..columnCount).toList().map { columnSize }
  val columns = arrangeHorizontal(0f, bounds, (1..columnCount).toList().map { columnSize })

  val creaturesOutline = { bounds2: Bounds ->
    verticalList((1..rowCount).toList(), bounds2, rowSize, 0f, { a, b -> listOf(border(b)) })
  }
  return columns.flatMap { creaturesOutline(it) }
}

fun battleView(state: ClientBattleState, world: World, bounds: Bounds): Layout {
  val columns = arrangeHorizontal(0f, bounds, (1..columnCount).toList().map { columnSize })
//  val columns = arrangeHorizontal(standardPadding, bounds, listOf(120f, null, 120f))
  return battleLayoutOutline(bounds)
      .plus(creaturesView(world, state, columns[0]))
      .plus(playerView(world.player, state, columns[3]))
}
