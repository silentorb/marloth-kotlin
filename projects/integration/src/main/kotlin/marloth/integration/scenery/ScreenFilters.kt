package marloth.integration.scenery

import silentorb.mythic.characters.rigs.ViewMode
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.ScreenFilter
import silentorb.mythic.spatial.Vector4
import simulation.combat.PlayerOverlayType
import simulation.main.Deck

val desaturation: ScreenFilter = { shaders, scale -> shaders.screenDesaturation.activate(scale) }

fun solidColorFilter(color: Vector4): ScreenFilter =
    { shaders, scale -> shaders.screenColor.activate(scale, color) }

fun bloodFilter(strength: Float): ScreenFilter =
    solidColorFilter(Vector4(1f, 0f, 0f, strength))

fun shadowSpiritFilter(strength: Float): ScreenFilter =
    solidColorFilter(Vector4(0.5f, 0f, 1f, strength))

fun getScreenFilters(deck: Deck, actor: Id): List<ScreenFilter> {
  val character = deck.characters[actor]!!
  if (deck.characterRigs[actor]!!.viewMode != ViewMode.firstPerson)
    return listOf()


  return if (!character.isAlive)
//    if (character.isInfinitelyFalling)
//      listOf(
//          desaturation,
//          solidColorFilter(Vector4(0f, 0f, 0f, 1f))
//      )
//    else
    listOf(
        desaturation,
        bloodFilter(0.4f)
    )
  else {
    val player = deck.players[actor]
    val shadowSpiritFilters = if (player != null && player.rig != actor)
      listOf(
//          desaturation,
          shadowSpiritFilter(0.3f)
      )
    else
      listOf()

    val damageFilters = deck.playerOverlays
        .filterValues { it.player == actor }
        .mapNotNull { overlay ->
          when (overlay.value.type) {
            PlayerOverlayType.bleeding -> bloodFilter(0.2f)
            else -> null
          }
        }

    return shadowSpiritFilters + damageFilters

  }
}
