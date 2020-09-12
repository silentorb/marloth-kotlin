package marloth.clienting.hud

import marloth.clienting.menus.TextResources
import marloth.clienting.menus.ViewId
import marloth.clienting.menus.black
import marloth.clienting.menus.TextStyles
import silentorb.mythic.bloom.*
import silentorb.mythic.characters.rigs.ViewMode
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.Id
import simulation.combat.general.ResourceContainer
import simulation.entities.Interactable
import simulation.main.World
import simulation.misc.getPointCell
import simulation.misc.getVictoryKeyStats
import kotlin.math.roundToInt

private val textStyle = TextStyles.smallGray

data class Cooldown(
    val name: String,
    val value: Float
)

fun floatToRoundedString(value: Float): String =
    ((value * 100).roundToInt().toFloat() / 100).toString()

fun resourceString(resource: ResourceContainer): String {
  val value = resource.value
  val max = resource.max
  return "$value / $max"
}

fun reverseResourceString(resource: ResourceContainer): String {
  val max = resource.max
  val value = max - resource.value
  return "$value / $max"
}

val df = java.text.DecimalFormat("#0.00")

private fun playerStats(world: World, actor: Id, debugInfo: List<String>): Flower {
  val deck = world.deck
  val destructible = deck.destructibles[actor]!!
  val character = deck.characters[actor]!!
  val accessoryPoints = character.accessoryPoints + if (character.accessoryOptions != null) 1 else 0
  val rows = listOf(
      label(textStyle, "Injury: ${reverseResourceString(destructible.health)}"),
      label(textStyle, "Doom: ${world.global.doom}")
//      label(textStyle, "Sanity: ${resourceString(data.madness)}")
  ) + listOfNotNull(
      if (accessoryPoints > 0) label(textStyle, "Ability Points: $accessoryPoints") else null
  ) + debugInfo.map {
    label(textStyle, it)
  }
  return reverseOffset(justifiedStart, justifiedEnd)(
      hudBox(
          boxList(verticalPlane, 10)(rows)
      )
  )
}

//private val interactionBar =
//    div("a",
//        forward = forwardOffset(top = percentage(0.8f)),
//        reverse = reverseOffset(left = centered) + reverseDimensions(height = shrinkWrap),
//        depiction = solidBackground(black)
//    )

fun interactionDialog(textResources: TextResources, interactable: Interactable): Flower {
  val secondary = interactable.secondaryCommand
  val rows = listOfNotNull(
      label(textStyle, textResources(interactable.primaryCommand.text) + " A"),
      if (secondary != null)
        label(textStyle, textResources(secondary.text) + " B")
      else
        null
  )

  val gap = 20
  throw Error("Need Updating")
//  return interactionBar(
//      div("b", reverse = reverseOffset(left = centered) + reverseDimensions(height = shrinkWrap))(
//          forwardMargin(top = gap, bottom = gap)(
//              boxToFlower(boxList(verticalPlane, gap)(rows))
//          )
//      )
//  )
}

fun hudLayout(textResources: TextResources, world: World, player: Id, view: ViewId?): Flower? {
  val deck = world.deck
  val definitions = world.definitions
  val grid = world.realm.grid

  val character = deck.characters[player]
  val characterRig = deck.characterRigs[player]
  return if (character == null)
    null
  else {
    val destructible = deck.destructibles[player]!!
    val body = deck.bodies[player]!!

    val accessories = deck.accessories
        .filter { it.value.owner == player }

    val interactable = if (view == null)
      deck.interactables[character.canInteractWith]
    else null

    val cooldowns = accessories
        .mapNotNull { (accessory, accessoryRecord) ->
          val cooldown = deck.actions[accessory]?.cooldown
          if (cooldown != null && cooldown != 0f) {
            val actionDefinition = definitions.actions[accessoryRecord.type]!!
            val accessoryDefinition = definitions.accessories[accessoryRecord.type]!!
            Cooldown(
                name = textResources(accessoryDefinition.name)!!,
                value = 1f - cooldown / actionDefinition.cooldown
            )
          } else
            null
        }
        .plus(
            accessories
                .mapNotNull { (id, accessory) ->
                  val timer = deck.timersFloat[id]
                  if (timer != null) {
                    val definition = definitions.accessories[accessory.type]
                    if (definition != null)
                      Cooldown(
                          name = textResources(definition.name)!!,
                          value = 1f - timer.duration / timer.original
                      )
                    else
                      null
                  } else
                    null
                }
        )

    val victoryKeyStats = getVictoryKeyStats(grid, deck)
    val viewMode = characterRig?.viewMode ?: ViewMode.firstPerson
    val cell = getPointCell(body.position)
    val debugInfo = listOfNotNull(
//            "LR: ${floatToRoundedString(lightRating(deck, player))}",
//            floatToRoundedString(body.velocity.length()),
        "Keys: ${victoryKeyStats.collected}/${victoryKeyStats.total}",
//            floatToRoundedString(deck.thirdPersonRigs[player]!!.rotation.x)
//            deck.characterRigs[player]!!.hoverCamera!!.pitch.toString()
        if (getDebugString() != "") getDebugString() else null,
        if (getDebugBoolean("HUD_DRAW_CELL_LOCATION")) "${cell.x} ${cell.y} ${cell.z}" else null,
        if (getDebugBoolean("HUD_DRAW_LOCATION"))
          "${floatToRoundedString(body.position.x)} ${floatToRoundedString(body.position.y)} ${floatToRoundedString(body.position.z)}"
        else
          null
//          if (character.isGrounded) "Grounded" else "Air",
//          character.groundDistance.toString()
    )
    compose(listOfNotNull(
        playerStats(world, player, debugInfo),
        if (interactable != null) interactionDialog(textResources, interactable) else null,
        if (cooldowns.any()) cooldownIndicatorPlacement(cooldowns) else null,
        if (viewMode == ViewMode.firstPerson) reticlePlacement() else null
    ))
  }
}
