package marloth.clienting.gui

import mythic.bloom.Bounds
import mythic.bloom.next.Box
import mythic.bloom.next.Flower
import mythic.bloom.textDepiction
import mythic.drawing.globalFonts
import mythic.ent.Id
import mythic.spatial.Vector2
import mythic.spatial.toVector2i
import mythic.typography.IndexedTextStyle
import mythic.typography.TextConfiguration
import mythic.typography.calculateTextDimensions
import mythic.typography.resolveTextStyle
import scenery.enums.Text
import simulation.main.Deck

fun getPlayerInteractingWith(deck: Deck): Id? =
    deck.characters[deck.players.keys.first()]!!.interactingWith

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
