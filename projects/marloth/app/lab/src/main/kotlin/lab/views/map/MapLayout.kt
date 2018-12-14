package lab.views.map

import lab.utility.white
import marloth.clienting.Client
import mythic.bloom.*
import mythic.drawing.grayTone
import mythic.ent.Id
import mythic.typography.IndexedTextStyle
import org.joml.Vector2i
import simulation.Node
import simulation.Realm

private val textStyle = IndexedTextStyle(0, 12f, grayTone(0.7f))

private val selectedTextStyle = IndexedTextStyle(0, 12f, white)

private val mainPanel: ParentFlower =
//    fixedList(horizontal, 0, listOf(null, 250))
    fixedList(horizontal, 0, listOf(null, 350))

private fun mapDisplay(client: Client, realm: Realm, config: MapViewConfig): Flower =
    depict(renderMapView(client, realm, config)) + logic(onClick("clickMap"))

private fun faceInfo(realm: Realm, id: Id): Flower {
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
      .map { mythic.bloom.label(textStyle, it) }

  return (mythic.bloom.offset(Vector2i(10)))(
      list(vertical, 10)(rows)
  )
}

private fun infoPanel(realm: Realm, config: MapViewConfig): Flower =
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
    list(
        children(lengthArranger(vertical, 15), nodeRow),
        selectable(nodeListSelectionKey, optionalSingleSelection) { it.id.toString() }
    )
)

private fun rightPanel(realm: Realm, config: MapViewConfig): Flower =
    fixedList(vertical, 10, listOf(400, null))(listOf(
        scrolling("infoPanel-scrolling")(infoPanel(realm, config)),
        nodeList(realm.nodeList)
    ))

fun mapLayout(client: Client, realm: Realm, config: MapViewConfig): Flower {
  return mainPanel(listOf(
      mapDisplay(client, realm, config),
      rightPanel(realm, config)
  ))
}