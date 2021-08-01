package marloth.integration.scenery

import silentorb.mythic.characters.rigs.ViewMode
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.lookinglass.ScreenFilter
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.timing.FloatTimer
import simulation.characters.ActivityEvents
import simulation.characters.CharacterActivity
import simulation.combat.PlayerOverlayType
import simulation.main.Deck

val desaturation: ScreenFilter = { shaders, scale -> shaders.screenDesaturation.activate(scale) }

fun solidColorFilter(color: Vector4): ScreenFilter =
    { shaders, scale -> shaders.screenColor.activate(scale, color) }

fun bloodFilter(strength: Float): ScreenFilter =
    solidColorFilter(Vector4(1f, 0f, 0f, strength))

fun shadowSpiritFilter(strength: Float): ScreenFilter =
    solidColorFilter(Vector4(0.5f, 0f, 1f, strength))

fun sleepFilter(strength: Float): ScreenFilter =
    solidColorFilter(Vector4(0f, 0f, 0f, strength))

fun getSleepFilter(deck: Deck, actor: Id, commandType: String, strength: (FloatTimer) -> Float): ScreenFilter? {
  val timer = deck.timersFloat.values.firstOrNull { timer ->
    timer.onFinished
        .any { event ->
          event is Command && event.type == commandType && event.target == actor
        }
  }
  return if (timer != null)
    sleepFilter(strength(timer))
  else
    null
}

fun getScreenFilters(deck: Deck, actor: Id): List<ScreenFilter> {
  val character = deck.characters[actor]!!
  if (deck.characterRigs[actor]!!.viewMode != ViewMode.firstPerson)
    return listOf()

  return if (!character.isAlive)
    listOf(
        desaturation,
        bloodFilter(0.4f)
    )
  else {
    val player = deck.players[actor]
    val shadowSpiritFilters = if (player != null && player.rig != actor)
      listOf(
          shadowSpiritFilter(0.25f)
      )
    else
      listOf()

    val damageFilters = deck.playerOverlays
        .filterValues { it.player == actor }
        .mapNotNull { overlay ->
          when (overlay.value.type) {
            PlayerOverlayType.bleeding -> bloodFilter(0.5f * overlay.value.strength)
            else -> null
          }
        }

    val sleepFilter = when (character.activity) {
      CharacterActivity.startingAbsence -> getSleepFilter(deck, actor, ActivityEvents.finishingAbsence) { 1f - it.duration / it.original }
      CharacterActivity.finishingAbsence -> getSleepFilter(deck, actor, ActivityEvents.finishedAbsence) { it.duration / it.original }
      else -> null
    }

    return shadowSpiritFilters + damageFilters + listOfNotNull(sleepFilter)
  }
}
