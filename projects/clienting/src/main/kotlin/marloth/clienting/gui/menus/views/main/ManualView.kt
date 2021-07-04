package marloth.clienting.gui.menus.views.main

import marloth.clienting.gui.Colors
import marloth.clienting.gui.StateFlower
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.dialogContentFlower
import marloth.clienting.gui.menus.dialogSurroundings
import marloth.clienting.gui.menus.dialogTitle
import marloth.clienting.gui.menus.views.options.formatBindingsText
import marloth.clienting.input.InputContext
import marloth.clienting.input.InputOptions
import marloth.clienting.input.defaultInputProfile
import org.commonmark.node.*
import org.commonmark.parser.Parser
import silentorb.mythic.bloom.*
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.haft.Bindings
import silentorb.mythic.lookinglass.toCamelCase
import silentorb.mythic.resource_loading.loadTextResource
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector2
import simulation.misc.Definitions

private val manualContentKey = "silentorb.content.manual"

fun circleDepiction(radius: Float, color: Vector4): Depiction = { bounds, canvas ->
  val position = bounds.position.toVector2() + bounds.dimensions.toVector2() / 2f
  canvas.drawCircle(position, radius, canvas.solid(color))
}

private val replacementPattern = Regex("""\$\$([\w\-]+)""")

fun applyBindingsText(definitions: Definitions, bindings: Bindings, text: String): String =
    text.replace(replacementPattern) { match ->
      val key = match.groups[1]?.value
      if (key == null)
        ""
      else {
        val command = toCamelCase(key)
        formatBindingsText(definitions, bindings.filter { it.command == command })
      }
    }

val bulletBox = depictBox(Vector2i(10, 16), circleDepiction(5f, Colors.black))

fun gatherLines(definitions: Definitions, bindings: Bindings, node: Node?, initialDepth: Int = 0): List<Flower> {
  val lines = mutableListOf<Flower>()
  var currentNode = node
  var depth = initialDepth
  var tabLength = 20
  while (currentNode != null) {
    when (currentNode) {
      is Paragraph, is ListItem ->
        lines += gatherLines(definitions, bindings, currentNode.firstChild, depth)
      is BulletList ->
        lines += flowerList(verticalPlane, 18)(
            gatherLines(definitions, bindings, currentNode.firstChild, depth)
                .map { flower ->
                  flowerMargin(left = (depth - 1) * tabLength + 10)(
                      flowerBoxList(horizontalPlane, 16)(listOf(
                          bulletBox,
                          flower,
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
            val header = textBox(textStyle, textNode.literal)
            val marginLeft = depth * tabLength
            val topMargin = if (currentNode.previous is Heading)
              10
            else when (currentNode.level) {
              2 -> 40
              else -> 30
            }
            lines += flowerMargin(left = marginLeft, top = topMargin, bottom = 22)(header)
          }
          depth = currentNode.level - 1
        }
      }
      is Text ->
        lines.add(textBox(TextStyles.smallBlack, applyBindingsText(definitions, bindings, currentNode.literal)))
    }
    currentNode = currentNode.next
  }
  return lines
}

fun formatDocument(definitions: Definitions, bindings: Bindings, document: Node): Flower {
  val node: Node? = document.firstChild
  val lines = gatherLines(definitions, bindings, node) +
      { _: Seed -> Box(dimensions = Vector2i(8, 10)) }

  return flowerList(verticalPlane, 0)(lines)
}

fun loadDocument(definitions: Definitions, bindings: Bindings, state: BloomState, key: String, filename: String): Flower {
  val existing = state[key] as? Flower
  return if (existing != null)
    existing
  else {
    val parser = Parser.builder().build()
    val text = loadTextResource(filename)
    if (text == null)
      textBox(TextStyles.mediumBlack, "Could not load content.")
    else {
      val document = parser.parse(text)
      formatDocument(definitions, bindings, document)
    }
  }
}

fun manualView(options: InputOptions): StateFlower = { definitions, state ->
  val bindings = options.profiles[defaultInputProfile]!!.bindings[InputContext.game]!!
  val content = loadDocument(definitions, bindings, state.bloomState, manualContentKey, "docs/manual.md")
  compose(
      dialogSurroundings(definitions),
      flowerMargin(all = 100)(
          alignSingleFlower(centered, horizontalPlane,
              dialogContentFlower(dialogTitle("Manual"))(
                  scrollableY("bindingsScrolling",
                      withLogic { _, _ ->
                        if (getDebugBoolean("DEBUG_MD_RENDERINg"))
                          mapOf()
                        else
                          mapOf(manualContentKey to content)
                      }(content)
                  )
              )
          )
      )
  )
}
