package marloth.clienting.gui.menus

import marloth.clienting.ClientEventType
import marloth.clienting.canvasRendererKey
import marloth.clienting.gui.ViewId
import marloth.clienting.resources.UiTextures
import silentorb.mythic.bloom.*
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.*
import simulation.accessorize.AccessoryDefinition
import simulation.main.Deck
import simulation.misc.Definitions

fun getPlayerInteractingWith(deck: Deck, player: Id): Id? =
    deck.characters[player]?.interactingWith

val debugDepiction = solidBackground(Vector4(1f, 0f, 0f, 1f))

typealias DepictionTransform = (Bounds, Canvas) -> Matrix

val basicDepictionTransform: DepictionTransform = { b, c ->
  c.transformScalar(b.position.toVector2(), b.dimensions.toVector2())
}

fun imageDepiction(texture: TextureName, depictionTransform: DepictionTransform = basicDepictionTransform): Depiction = { b, c ->
  val renderer = c.custom[canvasRendererKey]!! as Renderer
  val textureResource = renderer.textures[texture]
  if (textureResource != null) {
    val transform = depictionTransform(b, c)
    c.drawImage(transform, c.image(textureResource))
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
