package lab.views.map

import marloth.clienting.Client
import marloth.clienting.gui.TextStyles
import mythic.bloom.*
import mythic.bloom.next.*

import mythic.ent.Id
import simulation.Node
import simulation.Realm

private val textStyle = TextStyles.smallGray

private val selectedTextStyle = TextStyles.smallWhite

private val mapMenu: Flower = menuBar(textStyle, listOf(
    Menu(
        name = "View",
        character = "v",
        items = listOf(
            MenuItem(
                name = "Toggle normals"
            )
        )
    )
))

private val horizontalPanel: ParentFlower =
    fixedList(horizontalPlane, 0, listOf(null, 250))

private fun mapDisplay(client: Client, realm: Realm, config: MapViewConfig): Flower =
    depict(renderMapView(client, realm, config)) plusLogic onClick("clickMap")

private fun faceInfo(realm: Realm, id: Id): Flower {
  val connection = realm.faces[id]
  if (connection == null)
    return emptyFlower

  val face = realm.mesh.faces[id]!!
  val firstNode = realm.nodeTable[connection.firstNode]
  val secondNode = realm.nodeTable[connection.secondNode]
  val rows = listOfNotNull(
      id.toString(),
      connection.texture?.name ?: "no texture",
      "nodes: ${connection.firstNode}, ${connection.secondNode}",
      if (firstNode != null) "${firstNode.radius} ${firstNode.position}" else null,
      if (secondNode != null) "${secondNode.radius} ${secondNode.position}" else null,
      "neighbors: "
  )
      .plus(face.neighbors.map { "  ${it.id} ${realm.faces[it.id]!!.faceType}" })
      .plus("points:")
      .plus(face.vertices.map { "  " + it.toString() })
      .plus("-")
      .map { label(textStyle, it) }

  return margin(all = 10)(
      list(verticalPlane, 10)(rows)
  )
}

private fun infoPanel(realm: Realm, config: MapViewConfig): Flower =
    when {
      config.selection.any() -> faceInfo(realm, config.selection.first())
      else -> emptyFlower
    }

const val nodeListSelectionKey = "nodeList-selection"

private val nodeListSelectable = selectable<Node>(nodeListSelectionKey, optionalSingleSelection) { it.id.toString() }

private val nodeRow: (Node) -> Flower = { node ->
  depictSelectable(nodeListSelectionKey, node.id.toString()) { seed, selected ->
    val style = if (selected)
      selectedTextStyle
    else
      textStyle

    val solid = if (node.isSolid) "S" else " "
    val walkable = if (node.isWalkable) "W" else " "

    textDepiction(style, " ${node.id} $solid $walkable")
  } plusLogic nodeListSelectable(node)
}

private fun nodeList(nodes: List<Node>): Flower =
    scrolling("nodeList-scrolling")(
        list(verticalPlane, 10)(nodes.map { node ->
          nodeRow(node)
        })
//            children(lengthArranger(verticalPlane, 15), nodeRow),
//            nodeListSelectable{ it.id.toString() }
    )

private fun rightPanel(realm: Realm, config: MapViewConfig): Flower =
    list(verticalPlane, 10)(listOf(
        scrolling("infoPanel-scrolling")(infoPanel(realm, config)),
        nodeList(realm.nodeList)
    ))

fun mapLayout(client: Client, realm: Realm, config: MapViewConfig): Flower {
  return list(verticalPlane, 0)(listOf(
      mapMenu,
      horizontalPanel(listOf(
          mapDisplay(client, realm, config),
          rightPanel(realm, config)
      ))
  ))

}
