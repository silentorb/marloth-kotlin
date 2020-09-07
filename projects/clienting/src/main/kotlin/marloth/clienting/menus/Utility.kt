package marloth.clienting.menus

import marloth.clienting.canvasRendererKey
import silentorb.mythic.bloom.Bounds
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.Flower
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
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.toVector2
import simulation.main.Deck

fun getPlayerInteractingWith(deck: Deck, player: Id): Id? =
    deck.characters[player]?.interactingWith

const val textResourcesKey = "textResources"

fun localizedLabel(style: IndexedTextStyle, text: Text): Flower = { seed ->
  val textResources = seed.bag[textResourcesKey]!! as TextResources
  val content = textResources(text)!!
  val config = TextConfiguration(content, Vector2(), resolveTextStyle(globalFonts(), style))
  val dimensions = calculateTextDimensions(config)
  Box(
      name = if (content.length < 32) content else content.substring(0, 32),
      bounds = Bounds(
          dimensions = dimensions.toVector2i()
      ),
      depiction = textDepiction(style, content)
  )
}

fun imageElement(texture: TextureName): Flower = { seed ->
  Box(
      bounds = Bounds(
          dimensions = seed.dimensions
      ),
      depiction = { b, c ->
        val renderer = c.custom[canvasRendererKey]!! as Renderer
        val textureResource = renderer.textures[texture]
        if (textureResource != null) {
          c.drawImage(b.position.toVector2(), b.dimensions.toVector2(), c.image(textureResource))
        }
      }
  )
}
