package lab.views.map

import marloth.clienting.Client
import marloth.clienting.gui.TextStyles
import mythic.bloom.*

import mythic.ent.Id
import org.joml.Vector2i
import simulation.Node
import simulation.Realm

private val textStyle = TextStyles.gray

private val selectedTextStyle = TextStyles.smallWhite

private val mainPanel: ParentFlower =
//    fixedList(horizontal, 0, listOf(null, 250))
    fixedList(horizontalPlane, 0, listOf(null, 350))

private fun mapDisplay(client: Client, realm: Realm, config: MapViewConfig): FlowerOld =
    depict(renderMapView(client, realm, config)) + logic(onClick("clickMap"))

private fun faceInfo(realm: Realm, id: Id): FlowerOld {
  val connection = realm.faces[id]
  if (connection == null)
    return emptyFlower

  val face = realm.mesh.faces[id]!!

  val rows = listOf(
      id.toString(),
      "nodes: ${connection.firstNode}, ${connection.secondNode}",
      "neighbors: "
  )
      .plus(face.neighbors.map { "  ${it.id} ${realm.faces[it.id]!!.faceType}" })
      .plus("points:")
      .plus(face.vertices.map { "  " + it.toString() })
      .plus("-")
      .map { labelOld(textStyle, it) }

  return withOffset(Vector2i(10))(
      listOld(verticalPlane, 10)(rows)
  )
}

private fun infoPanel(realm: Realm, config: MapViewConfig): FlowerOld =
    when {
      config.selection.any() -> faceInfo(realm, config.selection.first())
      else -> emptyFlower
    }

private val nodeRow: ListItem<Node> = ListItem(20) { node ->
  depictSelectable(nodeListSelectionKey, node.id.toString()) { seed, selected ->
    val style = if (selected)
      selectedTextStyle
    else
      textStyle

    val solid = if (node.isSolid) "S" else " "
    val walkable = if (node.isWalkable) "W" else " "

    textDepiction(style, " ${node.id} $solid $walkable")
  }
}

const val nodeListSelectionKey = "nodeList-selection"

private val nodeList: ListFlower<Node> = wrap(
    scrolling("nodeList-scrolling"),
    listOld(
        children(lengthArranger(verticalPlane, 15), nodeRow),
        selectable(nodeListSelectionKey, optionalSingleSelection) { it.id.toString() }
    )
)

private fun rightPanel(realm: Realm, config: MapViewConfig): FlowerOld =
    fixedList(verticalPlane, 10, listOf(400, null))(listOf(
        scrolling("infoPanel-scrolling")(infoPanel(realm, config)),
        nodeList(realm.nodeList)
    ))

fun mapLayout(client: Client, realm: Realm, config: MapViewConfig): FlowerOld {
  return mainPanel(listOf(
      mapDisplay(client, realm, config),
      rightPanel(realm, config)
  ))
}