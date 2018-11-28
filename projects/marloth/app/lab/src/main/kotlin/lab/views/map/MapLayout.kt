package lab.views.map

import lab.utility.black
import lab.utility.depictScene
import lab.utility.white
import marloth.clienting.Client
import mythic.bloom.*
import org.joml.Vector2i
import simulation.Node
import simulation.Realm

private val textStyle = resolve(DeferredTextStyle(0, 12f, white))

private val mainPanel: ParentFlower =
    arrangeChildren(horizontal(0, listOf(null, 200)))

private fun mapDisplay(client: Client, realm: Realm, config: MapViewConfig): Flower =
    depictScene(renderMapView(client, realm, config))

private val nodeRow: ListItem<Node> = ListItem(20) { node ->
  depict(label(textStyle, node.id.toString()))
}

private val nodeList: ListFlower<Node> = wrap(scrolling, list(vertical(15), nodeRow))

private fun rightPanel(realm: Realm): Flower = nodeList(realm.nodeList)

fun mapLayout(dimensions: Vector2i, client: Client, realm: Realm, config: MapViewConfig): List<Box> {
  val bounds = Bounds(dimensions = dimensions)

  return mainPanel(bounds, listOf(
      mapDisplay(client, realm, config),
      rightPanel(realm)
  ))
}