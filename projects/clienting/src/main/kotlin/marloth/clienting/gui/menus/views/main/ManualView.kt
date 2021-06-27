package marloth.clienting.gui.menus.views.main

import marloth.clienting.gui.GuiState
import marloth.clienting.gui.StateFlower
import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.verticalList
import marloth.clienting.gui.menus.logic.menuLengthKey
import marloth.scenery.enums.TextId
import org.commonmark.node.*
import org.commonmark.parser.Parser
import silentorb.mythic.bloom.*
import silentorb.mythic.resource_loading.loadTextResource
import silentorb.mythic.spatial.Vector2i
import simulation.misc.Definitions

private val manualContentKey = "silentorb.content.manual"

fun gatherLines(node: Node?): List<Box> {
  val lines = mutableListOf<Box>()
  var currentNode = node
  while (currentNode != null) {
    when (currentNode) {
      is BulletList, is Paragraph, is ListItem, is Heading -> {
        lines += gatherLines(currentNode.firstChild)
      }
      is Text -> {
        lines.add(label(TextStyles.mediumBlack, currentNode.literal))
      }
    }
    currentNode = currentNode.next
  }
  return lines
}

fun formatDocument(document: Node): Box {
  val node: Node? = document.firstChild
  val lines = gatherLines(node)

  return verticalList(lines)
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
