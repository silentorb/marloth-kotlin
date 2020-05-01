package marloth.integration.clienting

import marloth.clienting.hud.Cooldown
import marloth.clienting.hud.HudData
import marloth.clienting.menus.TextResources
import marloth.clienting.menus.ViewId
import silentorb.mythic.characters.ViewMode
import silentorb.mythic.ent.Id
import simulation.main.World
import simulation.misc.getPointCell
import simulation.misc.getVictoryKeyStats
import kotlin.math.roundToInt

fun floatToRoundedString(value: Float): String =
    ((value * 100).roundToInt().toFloat() / 100).toString()

fun gatherHudData(world: World, textResources: TextResources, player: Id, view: ViewId): HudData? {
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

    val modifiers = deck.modifiers
        .filter { it.value.target == player }

    val buffs = modifiers
        .map { Pair(deck.modifiers[it.key]!!, deck.timersInt[it.key]?.duration ?: 0) }

    val interactable = if (view == ViewId.none)
      deck.interactables[character.canInteractWith]
    else null

    val cooldowns = deck.accessories
        .filter { it.value.owner == player }
        .mapNotNull { (accessory, accessoryRecord) ->
          val cooldown = deck.actions[accessory]?.cooldown
          if (cooldown != null && cooldown != 0f) {
            val actionDefinition = definitions.actions[accessoryRecord.type]!!
            val accessoryDefinition = definitions.accessories[accessoryRecord.type]!!
            Cooldown(
                name = textResources[accessoryDefinition.name]!!,
                value = 1f - cooldown / actionDefinition.cooldown
            )
          } else
            null
        }
        .plus(
            modifiers
                .mapNotNull { (id, modifier) ->
                  val timer = deck.timersFloat[id]
                  if (timer != null) {
                    Cooldown(
                        name = textResources[definitions.modifiers[modifier.type]!!.name]!!,
                        value = 1f - timer.duration / timer.original
                    )
                  }
                  else
                    null
                }
        )

    val victoryKeyStats = getVictoryKeyStats(grid, deck)

    val cell = getPointCell(body.position)
    HudData(
        health = destructible.health,
        sanity = character.sanity,
        interactable = interactable,
        cooldowns = cooldowns,
        viewMode = characterRig?.viewMode ?: ViewMode.firstPerson,
        buffs = buffs,
        debugInfo = listOf(
//            "LR: ${floatToRoundedString(lightRating(deck, player))}",
//            floatToRoundedString(body.velocity.length()),
            "Keys: ${victoryKeyStats.collected}/${victoryKeyStats.total}"
//            floatToRoundedString(deck.thirdPersonRigs[player]!!.rotation.x)
//            deck.characterRigs[player]!!.hoverCamera!!.pitch.toString()
//            "${cell.x} ${cell.y} ${cell.z}",
//            "${floatToRoundedString(body.position.x)} ${floatToRoundedString(body.position.y)} ${floatToRoundedString(body.position.z)}"
//          if (character.isGrounded) "Grounded" else "Air",
//          character.groundDistance.toString()
        )
    )
  }
}
