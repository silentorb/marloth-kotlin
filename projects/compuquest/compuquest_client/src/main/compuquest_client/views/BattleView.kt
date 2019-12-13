package compuquest_client.views

import compuquest_client.EntitySelectionEvent
import compuquest_client.EntityType
import compuquest_client.GlobalAbilityEvent
import compuquest_simulation.*
import compuquest_simulation.Id
import compuquest_simulation.logic.isReady
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.next.*
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.next.*

val columnCount = 4
val rowCount = 8
val columnSize = 320f / columnCount
val rowSize = 200f / rowCount

data class ClientBattleState(
    val selectedEntity: Id?,
    val flicker: Float
)

data class BattleInfo(
    val state: ClientBattleState,
    val inputFilter: (Any?) -> Any?
)

val elementColors = mapOf(
    Element.ethereal to lightBlue,
    Element.plant to lightGreen,
    Element.robot to lightRed
)

fun resourceView(resource: Resource) =
    label(
//        elementColors[resource.element]!!,
        textStyles.smallWhite, "" + resource.value + "/" + resource.max
    )

fun abilityEvent(creature: Creature, ability: Ability): Any? =
    if (isReady(creature, ability))
      if (ability.type.target == AbilityTarget.global)
        GlobalAbilityEvent(
            actionType = ability.type.action,
            abilityId = ability.id,
            actor = creature.id
        )
      else
        EntitySelectionEvent(EntityType.ability, ability.id)
    else
      null

fun selectedDepiction(info: BattleInfo, id: Id): Depiction? =
    if (info.state.selectedEntity == id)
      { bounds: Bounds, canvas: Canvas ->
        drawBorder(bounds, canvas, LineStyle(red, 1f))
      }
    else
      null

fun creatureView(info: BattleInfo): (Creature) -> Flower = { creature ->
  //  val columns = arrangeHorizontal(standardPadding, bounds, listOf(80f, null))
  val columns = if (isAlive(creature) || info.state.flicker > 0.4f)
    listOf<Flower>()
        .plus(label(textStyles.smallWhite, creature.type.name.take(8)))
        .plus(label(textStyles.smallWhite, creature.life.toString()))
//        .plus(if (info.state.selectedEntity != null)
//          listOf(Box(
//              bounds = bounds,
//              handler = info.inputFilter(EntitySelectionEvent(EntityType.creature, creature.id))
//          ))
//        else
//          listOf())
  else
    listOf()

  list(horizontalPlane, 10)(columns)
}

fun creaturesView(world: World, info: BattleInfo): Flower {
  return list(verticalPlane, 20)(world.enemies.map(creatureView(info)))
}

fun abilityView(info: BattleInfo, creature: Creature): (Ability) -> Flower = { ability ->
  val rows = listOf(
      label(textStyles.smallWhite, ability.type.name),
      label(textStyles.smallWhite, (0 until ability.cooldown).map { "* " }.joinToString())
//      Box(
//          bounds = bounds,
//          depiction = selectedDepiction(info, ability.id),
//          handler = info.inputFilter(abilityEvent(creature, ability))
//      )
  )

  list(verticalPlane, 10)(rows)
}

fun playerView(creature: Creature, info: BattleInfo): Flower {
//  val rows = arrangeVertical(0f, bounds, (1..rowCount).toList().map { rowSize })
//  val abilityBoxes = creature.abilities.zip(rows.drop(1), { a, b ->
//    abilityView(info, creature, a, b)
//  }).flatten()
//  return listOf(label(white, creature.life.toString(), rows[0])).plus(abilityBoxes)
  val rows = creature.abilities.map(abilityView(info, creature))
  return list(verticalPlane, 10)(rows)
}

fun battleLayoutOutline(): Flower {
  val border = { b: Bounds ->
    Box(
        bounds = b,
        depiction = { b2: Bounds, c: Canvas ->
          drawBorder(b2, c, LineStyle(grayTone(0.3f), 1f))
        }
    )
  }

//  val columns = arrangeHorizontal(0f, bounds, (1..columnCount).toList().map { columnSize })
//
//  val creaturesOutline = { bounds2: Bounds ->
//    verticalList((1..rowCount).toList(), bounds2, rowSize, 0f, { a, b -> listOf(border(b)) })
//  }
//  return columns.flatMap { creaturesOutline(it) }
  return emptyFlower
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

fun renderMissileAnimation(world: World, animation: Animation): Flower {
  val action = animation.action
  val actor = world.creatures[action.actor]!!
  val target = world.creatures[action.target]!!
  val ability = getAbility(actor, action.ability!!)
  val actorPosition = getPosition(world, actor) + positionOffset
  val targetPosition = getPosition(world, target) + positionOffset
  val missilePosition = tweenPosition(actorPosition, targetPosition, animation.progress)
  return margin(left = missilePosition.x.toInt(), top = missilePosition.y.toInt())(
//      div(layout = layoutDimensions(fixed(50), fixed(10)))(
      label(
          textStyles.smallWhite,
          ability.type.name
      )
//      )
  )
}

fun renderAnimation(world: World): Flower {
  val animation = world.animation
  return if (animation == null || animation.delay > 0f)
    emptyFlower
  else when (animation.action.type) {
    ActionType.attack -> renderMissileAnimation(world, animation)
    else -> emptyFlower
  }
}

fun isAcceptingInput(world: World): Boolean =
    world.activeCreature == world.player && world.animation == null && !isGameOver(world)

fun battleView(state: ClientBattleState, world: World): Flower {
//  val columns = arrangeHorizontal(0f, bounds, (1..columnCount).toList().map { columnSize })
//  val columns = arrangeHorizontal(standardPadding, bounds, listOf(120f, null, 120f))
  val info = BattleInfo(
      state = state,
      inputFilter = if (isAcceptingInput(world)) { it: Any? -> it } else { it: Any? -> null }
  )
  return compose(
      battleLayoutOutline(),
      creaturesView(world, info),
      playerView(world.player, info),
      renderAnimation(world)
  )
}
