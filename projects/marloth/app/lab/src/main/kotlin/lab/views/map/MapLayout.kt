package lab.views.map

import marloth.clienting.Client
import marloth.clienting.gui.textStyles
import mythic.bloom.*
import mythic.bloom.next.*

import mythic.ent.Id
import simulation.misc.Node
import simulation.misc.Realm

val mapTextStyle = textStyles.smallGray

private val selectedTextStyle = textStyles.smallWhite

private val horizontalPanel: ParentFlower =
    fixedList(horizontalPlane, 0, listOf(null, 250))

const val bagClickMap = "clickMap"

private fun mapDisplay(client: Client, realm: Realm, config: MapViewConfig): Flower =
    depict("map display", renderMapView(client, realm, config)) plusLogic onClick(bagClickMap)

private fun faceInfo(realm: Realm, id: Id): Flower {
  val connection = realm.faces[id]
  if (connection == null)
    return emptyFlower

//  val face = realm.mesh.faces[id]!!
  val firstNode = realm.nodeTable[connection.firstNode]
  val secondNode = realm.nodeTable[connection.secondNode]
  val rows = listOfNotNull(
      id.toString(),
      connection.texture ?: "no texture",
      "nodes: ${connection.firstNode}, ${connection.secondNode}",
      if (firstNode != null) "${firstNode.radius} ${firstNode.position}" else null,
      if (secondNode != null) "${secondNode.radius} ${secondNode.position}" else null,
      "neighbors: "
  )
//      .plus(face.neighbors.map { "  ${it.id} ${realm.faces[it.id]!!.faceType}" })
//      .plus("points:")
//      .plus(face.vertices.map { "  " + it.toString() })
//      .plus("-")
      .map { label(mapTextStyle, it) }

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

private val nodeListSelectable = selectable<Node>(nodeListSelectionKey, optionalSingleSelection) {
  it.id.toString()
}

private val nodeRow: (Node) -> Flower = { node ->
  selectableFlower(nodeListSelectionKey, node.id.toString()) { seed, selected ->
    val style = if (selected)
      selectedTextStyle
    else
      mapTextStyle

    val solid = if (node.isSolid) "S" else " "
    val walkable = if (node.isWalkable) "W" else " "

    label(style, " ${node.id} $solid $walkable")
  } plusLogic onClick(nodeListSelectable(node))
}

private fun nodeList(nodes: List<Node>): Flower =
    list(verticalPlane, 10)(nodes.map { node ->
      nodeRow(node)
    }) plusLogic persist(nodeListSelectionKey)

private fun rightPanel(realm: Realm, config: MapViewConfig): Flower =
    fixedList(verticalPlane, 10, listOf(250, null))(listOf(
        scrolling("infoPanel-scrolling")(infoPanel(realm, config)),
        scrolling("nodeList-scrolling")(nodeList(realm.nodeList))
    ))

fun mapLayout(client: Client, realm: Realm, config: MapViewConfig): Flower {
  return list(verticalPlane, drawReversed = true, name = "map-layout-root")(listOf(
      mapMenu,
      horizontalPanel(listOf(
          mapDisplay(client, realm, config),
          rightPanel(realm, config)
      ))
  ))

}
