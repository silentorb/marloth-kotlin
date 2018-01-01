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

fun prepareText(config: TextConfiguration, vertexSchema: VertexSchema): TextPackage? {
  val (content, font, size) = config
  val withoutSpaces = content.replace(" ", "")
  val characters = font.characters
  val inserted_newlines: MutableList<Int> = mutableListOf()
  var block_dimensionsX = 0f
  var block_dimensionsY = 0f


  val characterCount = withoutSpaces.length
  if (characterCount == 0) {
    return null
  }

//  val vertices = ArrayList<Vector4>(6 * element_count)
  val vertices = BufferUtils.createFloatBuffer(4 * characterCount * vertexSchema.floatSize)
  val offsets = BufferUtils.createIntBuffer(characterCount)
  val counts = BufferUtils.createIntBuffer(characterCount)
//  MemoryUtil.memSet(counts, 4)

  val letter_space = 6f
  val max_width = _max_width * unitConversion / size
  val line_step = characters['A']!!.info.sizeY * line_height
  var x = 0f
  var y = characters['A']!!.info.sizeY.toFloat()
  var following_visible_character = false
  block_dimensionsX = 0f
  var last_space_index = 0
  var last_space_x = 0f
  var step = 0
  val offset = -y * 2
  var index = 0

  for (i in 0 until content.length) {
    val c = content[i]
    if (c == ' ') {
      //        x += font.get_dimensions().x * 0.8f;
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
    val width = character.info.sizeX

    val height = character.info.sizeY.toFloat()
    val py = y - (character.info.bearingY - character.info.sizeY) + offset //-font.max_height*scale;

    val texture_width = (character.info.sizeX + 1).toFloat() / font.dimensions.x

    vertices.put(Vector4(x + width, py - height, texture_width, character.offset))
    vertices.put(Vector4(x + width, py, texture_width, character.offset + character.height))
    vertices.put(Vector4(x, py, 0f, character.offset + character.height))
    vertices.put(Vector4(x, py - height, 0f, character.offset))
    offsets.put(index)
    index += 4
    counts.put(4)
    x += width

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
  vertices.flip()
  offsets.flip()
  counts.flip()

  return TextPackage(
      SimpleMesh(vertexSchema, vertices, offsets, counts)
  )
}

/*
fun prepareText(content: String, font: Font) {

  val without_spaces = content.replace(" ", "")
  val characters = font.characters

  element_count = without_spaces.size()
  if (element_count === 0) {
    return
  }

  val vertices = ArrayList<Vector4>(6 * element_count)
  var left = 0f
  var step = 0
  var top = -characters.at('A').size.y
//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
  val offset = characters.at('A').size.y * 2
//    float top = 0;
  //            actual_height = font.characters['A'].size.y * line_size;

  var next_inserted_newline = 0
  var following_visible_character = false

  for (i in 0 until content.size()) {
    //C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
    val c = content[i]
    if (next_inserted_newline < inserted_newlines.size() && inserted_newlines[next_inserted_newline] === i) {
      ++next_inserted_newline
      top += characters.at('A').size.y * line_height
      left = 0f
      following_visible_character = false
      if (c == ' ') {
        continue
      }
    }

    if (c == ' ') {
      left += font.get_dimensions().x * 0.8f
      following_visible_character = false
      continue
    }

    if (c == '\n') {
      top += characters.at('A').size.y * line_height
      left = 0f
      following_visible_character = false
      continue
    }

    if (following_visible_character) {
      left += 6f
    }

    //C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
    val character = characters.at(c)
    val width = character.size.x
    val height = character.size.y
    val px = left
    val py = top - (character.bearing.y - character.size.y) + offset //-font.max_height*scale;

    //C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
    val texture_width = (character.size.x + 1) as Float / font.get_dimensions().x

    vertices.set(step + 5, Vector4(px, py, 0, character.offset + character.height))
    vertices.set(step + 4, Vector4(px, py - height, 0, character.offset))
    vertices.set(step + 3, Vector4(px + width, py - height, texture_width, character.offset))
    vertices.set(step + 2, Vector4(px + width, py, texture_width, character.offset + character.height))

    vertices.set(step + 1, vertices.get(step + 5))
    vertices.set(step + 0, vertices.get(step + 3))
    step += 6
    left += character.size.x
    following_visible_character = true
  }

//    block_dimensions.x = left;
//    block_dimensions.y = -top;
  mesh.replace(vertices.data() as Float, vertices.size)
//    glBufferData(GL_ARRAY_BUFFER, sizeof(float) * 6 * 4 * element_count, vertices, GL_DYNAMIC_DRAW);
//    glow::set_array_buffer(0);
  glow.check_error("Error preparing text.")
  appearance_changed = false

}
*/
