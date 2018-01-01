package mythic.glowing

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14.glMultiDrawArrays
import java.nio.FloatBuffer
import java.nio.IntBuffer

enum class DrawMethod {
  lineLoop,
  lines,
  lineStrip,
  points,
  triangles,
  triangleFan
}

fun createFloatBuffer(values: List<Float>): FloatBuffer {
  val buffer = BufferUtils.createFloatBuffer(values.size)
  for (value in values) {
    buffer.put(value)
  }
  buffer.flip()
  return buffer
}

fun createIntBuffer(value: Int): IntBuffer {
  val buffer = BufferUtils.createIntBuffer(1)
  buffer.put(value)
  buffer.flip()
  return buffer
}

class SimpleMesh(val vertexBuffer: VertexBuffer, val offsets: IntBuffer, val counts: IntBuffer) {

  private fun convertDrawMethod(method: DrawMethod): Int {
    when (method) {
      DrawMethod.triangles -> return GL_TRIANGLES
      DrawMethod.triangleFan -> return GL_TRIANGLE_FAN
      DrawMethod.lineLoop -> return GL_LINE_LOOP
      DrawMethod.lineStrip -> return GL_LINE_STRIP
      DrawMethod.lines -> return GL_LINES
      DrawMethod.points -> return GL_POINTS

      else -> {
        throw Error("Not supported.")
      }
    }
  }

  fun draw(method: DrawMethod) {
    vertexBuffer.activate()
    glMultiDrawArrays(convertDrawMethod(method), offsets, counts)
  }

  constructor(vertexSchema: VertexSchema, values: List<Float>) :
      this(VertexBuffer(
          createFloatBuffer(values), vertexSchema),
          createIntBuffer(0),
          createIntBuffer(values.size / vertexSchema.floatSize))

  constructor(vertexSchema: VertexSchema, buffer: FloatBuffer, offsets: IntBuffer, counts: IntBuffer) :
      this(VertexBuffer(buffer, vertexSchema), offsets, counts)

  fun dispose() {
    vertexBuffer.dispose()
  }
}