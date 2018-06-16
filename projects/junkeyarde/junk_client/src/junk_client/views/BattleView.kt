package junk_client.views

import junk_client.EntitySelectionEvent
import junk_client.EntityType
import junk_simulation.*
import junk_simulation.logic.isReady
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.spatial.Vector2
import mythic.spatial.times
import org.joml.minus
import org.joml.plus

val columnCount = 4
val rowCount = 8
val columnSize = 320f / columnCount
val rowSize = 200f / rowCount

data class ClientBattleState(
    val selectedEntity: Id?,
    val flicker: Float
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
      EntitySelectionEvent(EntityType.ability, ability.id)
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
  return if (isDead(creature))
    listOf()
  else
    listOf<Box>()
        .plus(label(white, creature.type.name.take(8), columns[0]))
        .plus(label(white, creature.life.toString(), columns[1]))
        .plus(if (state.selectedEntity != null)
          listOf(Box(
              bounds = bounds,
              handler = EntitySelectionEvent(EntityType.creature, creature.id)
          ))
        else
          listOf())
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

  val columns = arrangeHorizontal(0f, bounds, (1..columnCount).toList().map { columnSize })

  val creaturesOutline = { bounds2: Bounds ->
    verticalList((1..rowCount).toList(), bounds2, rowSize, 0f, { a, b -> listOf(border(b)) })
  }
  return columns.flatMap { creaturesOutline(it) }
}


fun tweenPosition(first: Vector2, second: Vector2, progress: Float): Vector2 {
  val direction = second - first
  return first + direction * progress
}

fun getPosition(world: World, creature: Creature): Vector2 =
    if (isPlayer(creature))
      Vector2(columnSize * 2f, 0f)
    else
      Vector2(0f, world.enemies.indexOf(creature) * rowSize)

private val positionOffset = Vector2(10f, 5f)

fun renderMissileAnimation(world: World, animation: Animation): Layout {
  val action = animation.action
  val actor = world.creatures[action.actor]!!
  val target = world.creatures[action.target]!!
  val ability = actor.abilities.first { it.id == action.ability }
  val actorPosition = getPosition(world, actor) + positionOffset
  val targetPosition = getPosition(world, target) + positionOffset
  val missilePosition = tweenPosition(actorPosition, targetPosition, animation.progress)
  return listOf(
      label(
          white,
          ability.type.name,
          Bounds(
              position = missilePosition,
              dimensions = Vector2(50f, 10f)
          )
      )
  )
}

fun renderAnimation(world: World): Layout {
  val animation = world.animation
  return if (animation == null || animation.delay > 0f)
    listOf()
  else when (animation.type) {
    AnimationType.missile -> renderMissileAnimation(world, animation)
    else -> listOf()
  }
}

fun battleView(state: ClientBattleState, world: World, bounds: Bounds): Layout {
  val columns = arrangeHorizontal(0f, bounds, (1..columnCount).toList().map { columnSize })
//  val columns = arrangeHorizontal(standardPadding, bounds, listOf(120f, null, 120f))
  return battleLayoutOutline(bounds)
      .plus(creaturesView(world, state, columns[0]))
      .plus(playerView(world.player, state, columns[3]))
      .plus(renderAnimation(world))
}
