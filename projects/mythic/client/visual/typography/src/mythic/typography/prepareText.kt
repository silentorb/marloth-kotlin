package mythic.typography

import mythic.glowing.SimpleMesh
import mythic.glowing.VertexSchema
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.spatial.put
import org.lwjgl.BufferUtils

private val _max_width = 0f
val unitConversion = 24f
private val line_height = 2f

data class TextConfiguration(
    val content: String,
    val font: Font,
    val size: Float,
    val position: Vector2,
    val color: Vector4
)

data class TextPackage(
    val mesh: SimpleMesh
)

data class ArrangedCharacter(
    val glyph: Glyph,
    val x: Float,
    val y: Float
)

data class TypeArrangement(
    val characters: List<ArrangedCharacter>
)

fun arrangeType(config: TextConfiguration): TypeArrangement? {
  val content = config.content
  val font = config.font
  val size = config.size
  val characters = font.characters
  val inserted_newlines: MutableList<Int> = mutableListOf()
  var block_dimensionsX = 0f
  var block_dimensionsY = 0f

  val characterCount = content.replace(" ", "").length
  if (characterCount == 0) {
    return null
  }

  val arrangedCharacters = ArrayList<ArrangedCharacter>(characterCount)

  val letter_space = 6f
  val max_width = _max_width * unitConversion / size
  val line_step = config.font.height * line_height
  var x = 0f
  var y = config.font.height
  var following_visible_character = false
  block_dimensionsX = 0f
  var last_space_index = 0
  var last_space_x = 0f

  for (i in 0 until content.length) {
    val c = content[i]
    if (c == ' ') {
      last_space_x = x
      last_space_index = i
      x += font.dimensions.x
      following_visible_character = false
      continue
    }

    if (c == '\n') {
      if (x > block_dimensionsX) {
        block_dimensionsX = x
      }

      y += line_step
      x = 0f
      following_visible_character = false
      last_space_index = 0
      last_space_x = 0f
      continue
    }

    if (following_visible_character) {
      x += letter_space
    }

    val character = characters[c]!!

    arrangedCharacters.add(ArrangedCharacter(
        character,
        x,
        y
    ))
    x += character.info.sizeX

    if (_max_width != 0f && x > max_width && last_space_index > 0) {
      if (last_space_x > block_dimensionsX) {
        block_dimensionsX = last_space_x
      }

      inserted_newlines.add(last_space_index)
      y += line_step
      x -= last_space_x
      last_space_index = 0
      last_space_x = 0f
    }

    following_visible_character = true
  }

  if (x > block_dimensionsX) {
    block_dimensionsX = x
  }

  block_dimensionsY = y

  return TypeArrangement(
      arrangedCharacters
  )
}

fun prepareText(config: TextConfiguration, vertexSchema: VertexSchema): TextPackage? {
  val arrangement = arrangeType(config)
  if (arrangement == null)
    return null

  val characters = arrangement.characters
  val vertices = BufferUtils.createFloatBuffer(4 * characters.size * vertexSchema.floatSize)
  val offsets = BufferUtils.createIntBuffer(characters.size)
  val counts = BufferUtils.createIntBuffer(characters.size)

  var index = 0

  for (arrangedCharacter in arrangement.characters) {
    val glyph = arrangedCharacter.glyph
    val x = arrangedCharacter.x
    val y = arrangedCharacter.y
    val width = glyph.info.sizeX
    val height = glyph.info.sizeY.toFloat()
    val texture_width = (glyph.info.sizeX + 1).toFloat() / config.font.dimensions.x

    vertices.put(Vector4(x + width, y - height, texture_width, glyph.offset))
    vertices.put(Vector4(x + width, y, texture_width, glyph.offset + glyph.height))
    vertices.put(Vector4(x, y, 0f, glyph.offset + glyph.height))
    vertices.put(Vector4(x, y - height, 0f, glyph.offset))
    offsets.put(index)
    index += 4
    counts.put(4)
  }

  vertices.flip()
  offsets.flip()
  counts.flip()

  return TextPackage(
      SimpleMesh(vertexSchema, vertices, offsets, counts)
  )
}