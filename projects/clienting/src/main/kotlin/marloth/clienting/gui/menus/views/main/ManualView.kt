package marloth.clienting.gui.menus.views.main

import marloth.clienting.gui.Colors
import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.dialogContentFlower
import marloth.clienting.gui.menus.dialogSurroundings
import marloth.clienting.gui.menus.dialogTitle
import marloth.clienting.gui.menus.general.verticalList
import org.commonmark.node.*
import org.commonmark.parser.Parser
import silentorb.mythic.bloom.*
import silentorb.mythic.resource_loading.loadTextResource
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector2

private val manualContentKey = "silentorb.content.manual"

fun circleDepiction(radius: Float, color: Vector4): Depiction = { bounds, canvas ->
  val position = bounds.position.toVector2() + bounds.dimensions.toVector2() / 2f
  canvas.drawCircle(position, radius, canvas.solid(color))
}

fun gatherLines(node: Node?): List<Box> {
  val lines = mutableListOf<Box>()
  var currentNode = node
  while (currentNode != null) {
    when (currentNode) {
      is Paragraph, is ListItem ->
        lines += gatherLines(currentNode.firstChild)
      is BulletList ->
        lines += gatherLines(currentNode.firstChild)
            .map { box ->
              horizontalList(20)(listOf(
                  depictBox(Vector2i(10, 16), circleDepiction(5f, Colors.black)),
                  box,
              ))
            }
      is Heading -> {
        if (lines.any()) {
          lines += Box(dimensions = Vector2i(10, 1))
        }
        val textNode = currentNode.firstChild as? Text
        if (textNode != null) {
          lines += label(TextStyles.mediumBlackBold, textNode.literal)
        }
      }
      is Text ->
        lines.add(label(TextStyles.smallBlack, currentNode.literal))
    }
    currentNode = currentNode.next
  }
  return lines
}

fun formatDocument(document: Node): Box {
  val node: Node? = document.firstChild
  val lines = gatherLines(node)

  return boxList(verticalPlane, 16)(lines)
}

fun loadDocument(state: BloomState, key: String, filename: String): Box {
  val existing = state[key] as? Box
  return if (existing != null)
    existing
  else {
    val parser = Parser.builder().build()
    val text = loadTextResource(filename)
    if (text == null)
      label(TextStyles.mediumBlack, "Could not load content.")
    else {
      val document = parser.parse(text)
      formatDocument(document)
    }
  }
}

fun manualView(): StateFlowerTransform = { definitions, state ->
  val content = loadDocument(state.bloomState, manualContentKey, "docs/manual.md")
  compose(
      dialogSurroundings(definitions),
      flowerMargin(all = 50)(
          alignSingleFlower(centered, horizontalPlane,
              dialogContentFlower(dialogTitle("Manual"))(
                  scrollableY("bindingsScrolling",
                      { seed ->
                        content
                            .addLogic { input, _ ->
                              mapOf()
//                mapOf(manualContentKey to content)
                            }
                      }
                  )
              )
          )
      )
  )
}
