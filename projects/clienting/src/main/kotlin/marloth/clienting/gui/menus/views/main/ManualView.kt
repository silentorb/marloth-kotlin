package marloth.clienting.gui.menus.views.main

import marloth.clienting.gui.Colors
import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.dialogContentFlower
import marloth.clienting.gui.menus.dialogSurroundings
import marloth.clienting.gui.menus.dialogTitle
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

fun gatherLines(node: Node?, initialDepth: Int = 0): List<Box> {
  val lines = mutableListOf<Box>()
  var currentNode = node
  var depth = initialDepth
  var tabLength = 20
  while (currentNode != null) {
    when (currentNode) {
      is Paragraph, is ListItem ->
        lines += gatherLines(currentNode.firstChild, depth)
      is BulletList ->
        lines += boxList(verticalPlane, 18)(
            gatherLines(currentNode.firstChild, depth)
                .map { box ->
                  boxMargin(left = (depth - 1) * tabLength + 10)(
                      horizontalList(16)(listOf(
                          depictBox(Vector2i(10, 16), circleDepiction(5f, Colors.black)),
                          box,
                      ))
                  )
                }
        )
      is Heading -> {
        if (currentNode.level > 1) { // Skip the title Header because the view already has a title header
          depth = currentNode.level - 2
          val textNode = currentNode.firstChild as? Text
          if (textNode != null) {
            val textStyle = when (currentNode.level) {
              2 -> TextStyles.mediumSemiBlackBold
              else -> TextStyles.h3
            }
            val header = label(textStyle, textNode.literal)
            val marginLeft = depth * tabLength
            val topMargin = if (currentNode.previous is Heading)
              10
            else when (currentNode.level) {
              2 -> 40
              else -> 30
            }
            lines += boxMargin(left = marginLeft, top = topMargin, bottom = 22)(header)
          }
          depth = currentNode.level - 1
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
  val lines = gatherLines(node) + Box(dimensions = Vector2i(8, 10))

  return boxList(verticalPlane, 0)(lines)
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
