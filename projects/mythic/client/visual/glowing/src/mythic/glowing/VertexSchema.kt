package mythic.glowing

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glGenVertexArrays

class VertexSchema(val attributes: List<VertexAttribute>) {
  val floatSize = attributes.sumBy { it.size }
}

class VertexArrayObject() {
  val id = glGenVertexArrays()

  companion object {

    fun createInterwoven(schema: VertexSchema): VertexArrayObject {
      val result = VertexArrayObject()
      var offset = 0L
      var i = 0
      globalState.vertexArrayObject = result.id

      for (attribute in schema.attributes) {
        glVertexAttribPointer(i, attribute.size, GL_FLOAT, false, schema.floatSize * 4, offset)
        glEnableVertexAttribArray(i)
        i++
        offset += attribute.size * 4
      }
      return result
    }

//    fun createStriding(schema: VertexSchema): VertexArrayObject {
//      val result = VertexArrayObject()
//      var offset = 0L
//      var i = 0
//      globalState.vertexArrayObject = result.id
//
//      for (attribute in schema.attributes) {
//        glVertexAttribPointer(i, attribute.size, GL_FLOAT, false, schema.floatSize * 4, offset)
//        glEnableVertexAttribArray(i)
//        i++
//        offset += attribute.size * 4
//      }
//      return result
//    }
  }

  fun activate() {
    globalState.vertexArrayObject = id
  }
}