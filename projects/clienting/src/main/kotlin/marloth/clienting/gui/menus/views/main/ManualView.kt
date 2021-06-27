package marloth.clienting.gui.menus.views.main

import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.dialogContent
import marloth.clienting.gui.menus.dialogTitle
import marloth.clienting.gui.menus.dialogWrapperWithExtras
import marloth.clienting.gui.menus.general.verticalList
import org.commonmark.node.*
import org.commonmark.parser.Parser
import silentorb.mythic.bloom.BloomState
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.label
import silentorb.mythic.resource_loading.loadTextResource

private val manualContentKey = "silentorb.content.manual"

fun gatherLines(node: Node?): List<Box> {
  val lines = mutableListOf<Box>()
  var currentNode = node
  while (currentNode != null) {
    when (currentNode) {
      is BulletList, is Paragraph, is ListItem, is Heading -> {
        gatherLines(currentNode.firstChild)
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

fun manualView() =
    dialogWrapperWithExtras(
        { definitions, state ->
          val content = loadDocument(state.bloomState, manualContentKey, "docs/manual.md")
          dialogContent(dialogTitle("Manual"))(
              content
          )
              .addLogic { input, _ ->
                mapOf()
//                mapOf(manualContentKey to content)
              }
        }
    )
