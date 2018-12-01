package lab.views.map

import lab.utility.depictScene
import lab.utility.white
import marloth.clienting.Client
import mythic.bloom.*
import mythic.drawing.grayTone
import simulation.Node
import simulation.Realm

private val textStyle = resolve(DeferredTextStyle(0, 12f, grayTone(0.7f)))

private val selectedTextStyle = resolve(DeferredTextStyle(0, 12f, white))

private val mainPanel: ParentFlower =
    applyBounds(horizontal(0, listOf(null, 200)))

private fun mapDisplay(client: Client, realm: Realm, config: MapViewConfig): Flower =
    depictScene(renderMapView(client, realm, config))

private val nodeRow: ListItem<Node> = ListItem(20) { node ->
  depictSelectable(nodeListSelectionKey, node.id.toString()) { seed, selected ->
    val style = if (selected)
      selectedTextStyle
    else
      textStyle

    label(style, node.id.toString())
  }
}

//private val selectableNodes = selectable<Node>(optionalSingleSelection) { it.id.toString() }

const val nodeListSelectionKey = "nodeList-selection"

private val nodeList: ListFlower<Node> = wrap(
    scrolling("nodeList-scrolling"),
    list(
        children(vertical(15), nodeRow),
        selectable(nodeListSelectionKey, optionalSingleSelection) { it.id.toString() }
    )
)

private fun rightPanel(realm: Realm): Flower = nodeList(realm.nodeList)

fun mapLayout(client: Client, realm: Realm, config: MapViewConfig): Flower {
  return mainPanel(listOf(
      mapDisplay(client, realm, config),
      rightPanel(realm)
  ))
}