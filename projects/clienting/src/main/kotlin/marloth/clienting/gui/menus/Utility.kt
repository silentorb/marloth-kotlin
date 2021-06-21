package marloth.clienting.gui.menus

import marloth.clienting.ClientEventType
import marloth.clienting.canvasRendererKey
import marloth.clienting.gui.ViewId
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.Depiction
import silentorb.mythic.bloom.Flower
import silentorb.mythic.bloom.solidBackground
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector2
import simulation.accessorize.AccessoryDefinition
import simulation.main.Deck
import simulation.misc.Definitions

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

fun redirectBox(view: ViewId?) =
    Box(
        dimensions = Vector2i.zero,
        handler = { _, _ -> listOf(Command(ClientEventType.navigate, view)) },
    )

fun redirectFlower(view: ViewId?): Flower = { dimensions ->
  redirectBox(view)
}

fun actionItemText(definitions: Definitions, accessoryDefinition: AccessoryDefinition, quantity: Int): String {
  val quantityClause = if (quantity > 1)
    " ($quantity)"
  else
    ""
  return definitions.textLibrary(accessoryDefinition.name) + quantityClause
}
