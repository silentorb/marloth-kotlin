package mythic.glowing

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glGenVertexArrays

class VertexSchema<T>(inputAttributes: List<VertexAttribute<T>>) {
  val floatSize = inputAttributes.sumBy { it.size }
  val attributes: List<VertexAttributeDetail<T>>

  init {
    var offset = 0
    attributes = inputAttributes.mapIndexed { i, input ->
      val result = VertexAttributeDetail(i, input.name, offset, input.size)
      offset += input.size
      result
    }
  }

  fun getAttribute(name: T) = attributes.first({ it.name == name })
}

class VertexArrayObject() {
  val id = glGenVertexArrays()

  companion object {

    fun <T> createInterwoven(schema: VertexSchema<T>): VertexArrayObject {
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