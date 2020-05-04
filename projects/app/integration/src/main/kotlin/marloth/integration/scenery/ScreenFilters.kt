package marloth.integration.scenery

import silentorb.mythic.characters.rigs.ViewMode
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.ScreenFilter
import silentorb.mythic.spatial.Vector4
import simulation.combat.PlayerOverlayType
import simulation.main.Deck

fun solidColorFilter(color: Vector4): ScreenFilter =
    { shaders, scale -> shaders.screenColor.activate(scale, color) }

fun bloodFilter(strength: Float): ScreenFilter =
    solidColorFilter(Vector4(1f, 0f, 0f, strength))

fun getScreenFilters(deck: Deck, player: Id): List<ScreenFilter> {
  val character = deck.characters[player]!!
  if (deck.characterRigs[player]!!.viewMode != ViewMode.firstPerson)
    return listOf()

  return if (!character.isAlive)
    if (character.isInfinitelyFalling)
      listOf<ScreenFilter>(
          { shaders, scale -> shaders.screenDesaturation.activate(scale) },
          solidColorFilter(Vector4(0f, 0f, 0f, 1f))
      )
    else
      listOf<ScreenFilter>(
          { shaders, scale -> shaders.screenDesaturation.activate(scale) },
          bloodFilter(0.4f)
      )
  else
    deck.playerOverlays
        .filterValues { it.player == player }
        .mapNotNull { overlay ->
          when (overlay.value.type) {
            PlayerOverlayType.bleeding -> bloodFilter(0.2f)
            else -> null
          }
        }
}
