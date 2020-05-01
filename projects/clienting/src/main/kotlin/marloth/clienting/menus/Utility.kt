package marloth.clienting.menus

import silentorb.mythic.bloom.Bounds
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.next.Flower
import silentorb.mythic.bloom.textDepiction
import silentorb.mythic.drawing.globalFonts
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.toVector2i
import silentorb.mythic.typography.IndexedTextStyle
import silentorb.mythic.typography.TextConfiguration
import silentorb.mythic.typography.calculateTextDimensions
import silentorb.mythic.typography.resolveTextStyle
import marloth.scenery.enums.Text
import simulation.main.Deck

fun getPlayerInteractingWith(deck: Deck, player: Id): Id? =
    deck.characters[player]?.interactingWith

const val textResourcesKey = "textResources"

fun localizedLabel(style: IndexedTextStyle, text: Text): Flower = { seed ->
  val textResources = seed.bag[textResourcesKey]!! as TextResources
  val content = textResources[text]!!
  val config = TextConfiguration(content, Vector2(), resolveTextStyle(globalFonts(), style))
  val dimensions = calculateTextDimensions(config)
  Box(
      bounds = Bounds(
          dimensions = dimensions.toVector2i()
      ),
      depiction = textDepiction(style, content),
      name = if (content.length < 32) content else content.substring(0, 32)
  )
}
