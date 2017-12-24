package glowing

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glGenVertexArrays

class VertexSchema(val attributes: Array<VertexAttribute>) {
  val size = attributes.sumBy { it.size }
}

class VertexArrayObject(schema: VertexSchema) {
  val id = glGenVertexArrays()

  init {
    var offset = 0L
    var i = 0
    globalState.vertexArrayObject = id

    for (attribute in schema.attributes) {
      glVertexAttribPointer(i, attribute.size, GL_FLOAT, false, schema.size, offset)
      glEnableVertexAttribArray(i)
      i++
      offset += attribute.size * 4
    }
  }

  fun activate() {
    globalState.vertexArrayObject = id
  }
}