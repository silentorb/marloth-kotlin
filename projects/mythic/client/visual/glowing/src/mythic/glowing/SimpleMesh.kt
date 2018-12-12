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

interface Drawable {
  fun draw(method: DrawMethod)
  fun dispose()
}

class SimpleMesh<T>(val vertexBuffer: VertexBuffer<T>, val offsets: IntBuffer, val counts: IntBuffer) : Drawable {

  override fun draw(method: DrawMethod) {
    vertexBuffer.activate()
    glMultiDrawArrays(convertDrawMethod(method), offsets, counts)
  }

  fun drawElement(method: DrawMethod, index: Int) {
    vertexBuffer.activate()
    glDrawArrays(convertDrawMethod(method), offsets[index], counts[index])
  }

  constructor(vertexSchema: VertexSchema<T>, values: List<Float>) :
      this(VertexBuffer(vertexSchema),
          createIntBuffer(0),
          createIntBuffer(values.size / vertexSchema.floatSize)) {
    vertexBuffer.load(createFloatBuffer(values))
  }

  constructor(vertexSchema: VertexSchema<T>, buffer: FloatBuffer, offsets: IntBuffer, counts: IntBuffer) :
      this(VertexBuffer(vertexSchema), offsets, counts) {
    vertexBuffer.load(buffer)
  }

  override fun dispose() {
    vertexBuffer.dispose()
  }
}

class SimpleTriangleMesh<T>(val vertexBuffer: VertexBuffer<T>, val indices: IntBuffer) : Drawable {

  override fun draw(method: DrawMethod) {
    vertexBuffer.activate()
    val convertedMethod = when(method) {
      DrawMethod.triangleFan -> DrawMethod.triangles
      DrawMethod.lineLoop -> DrawMethod.lineStrip
      else -> method
    }
    glDrawElements(convertDrawMethod(convertedMethod), indices)
  }

  constructor(vertexSchema: VertexSchema<T>, buffer: FloatBuffer, indices: IntBuffer) :
      this(VertexBuffer(vertexSchema), indices) {
    vertexBuffer.load(buffer)
  }

  override fun dispose() {
    vertexBuffer.dispose()
  }
}

class MutableSimpleMesh<T>(val vertexSchema: VertexSchema<T>) : Drawable {
  var offsets: IntBuffer = BufferUtils.createIntBuffer(1)
  var counts: IntBuffer = BufferUtils.createIntBuffer(1)
  val vertexBuffer = VertexBuffer(vertexSchema)
  val floatBuffer = BufferUtils.createFloatBuffer(64)
  val custodian = FloatBufferCustodian(floatBuffer)

  override fun draw(method: DrawMethod) {
    vertexBuffer.activate()
    glMultiDrawArrays(convertDrawMethod(method), offsets, counts)
  }

  fun load(values: List<Float>) {
    for (value in values) {
      floatBuffer.put(value)
    }
    custodian.finish()
    vertexBuffer.load(floatBuffer)
    offsets.put(0)
    offsets.flip()
    counts.put(values.size / vertexSchema.floatSize)
    counts.flip()
  }

  override fun dispose() {
    vertexBuffer.dispose()
  }
}