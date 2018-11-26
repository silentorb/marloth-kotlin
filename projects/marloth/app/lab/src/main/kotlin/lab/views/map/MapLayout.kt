package lab.views.map

import lab.utility.depictScene
import marloth.clienting.Client
import mythic.bloom.*
import org.joml.Vector2i
import simulation.Realm

private val mainPanel: ParentFlower =
    arrangeChildren(horizontal(0, listOf(null, 200)))

private fun mapDisplay(client: Client, realm: Realm, config: MapViewConfig): Flower =
    depictScene(renderMapView(client, realm, config))

private fun rightPanel(): Flower = { b ->
  listOf()
}

fun mapLayout(dimensions: Vector2i, client: Client, realm: Realm, config: MapViewConfig): List<Box> {
  val bounds = Bounds(dimensions = dimensions)

  return mainPanel(bounds, listOf(
      mapDisplay(client, realm, config),
      rightPanel()
  ))
}