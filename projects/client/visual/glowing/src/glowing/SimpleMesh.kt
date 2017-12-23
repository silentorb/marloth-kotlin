package glowing

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14.glMultiDrawArrays
import java.nio.IntBuffer

enum class DrawMethod {
  lineLoop,
  lines,
  lineStrip,
  points,
  triangles,
  triangleFan
}

class SimpleMesh(val vertexSchema: VertexSchema, val vertexBuffer: VertexBuffer,
                 val offsets: IntBuffer, val counts: IntBuffer) {

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
    vertexSchema.activate()
    vertexBuffer.activate()
    glMultiDrawArrays(convertDrawMethod(method), offsets, counts);
  }
}