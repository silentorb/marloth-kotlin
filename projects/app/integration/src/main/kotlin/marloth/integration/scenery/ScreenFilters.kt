package marloth.integration.scenery

import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.ScreenFilter
import silentorb.mythic.spatial.Vector4
import simulation.combat.PlayerOverlayType
import simulation.main.Deck

fun bloodFilter(strength: Float): ScreenFilter =
    { shaders, scale -> shaders.screenColor.activate(scale, Vector4(1f, 0f, 0f, strength)) }

fun getScreenFilters(deck: Deck, player: Id) =
    if (!deck.characters[player]!!.isAlive)
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
