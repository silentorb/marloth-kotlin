package marloth.clienting.gui.menus

import marloth.clienting.canvasRendererKey
import silentorb.mythic.ent.Id
import silentorb.mythic.bloom.*
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector2
import simulation.main.Deck

fun getPlayerInteractingWith(deck: Deck, player: Id): Id? =
    deck.characters[player]?.interactingWith

val debugDepiction = solidBackground(Vector4(1f, 0f, 0f, 1f))

fun imageDepiction(texture: TextureName): Depiction = { b, c ->
  val renderer = c.custom[canvasRendererKey]!! as Renderer
  val textureResource = renderer.textures[texture]
  if (textureResource != null) {
    c.drawImage(b.position.toVector2(), b.dimensions.toVector2(), c.image(textureResource))
  }
}
