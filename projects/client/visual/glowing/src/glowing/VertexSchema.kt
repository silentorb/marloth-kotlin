package glowing

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays

class VertexSchema(val attributes: Array<VertexAttribute>) {
  val size = attributes.sumBy { it.size }
  val vao = glGenVertexArrays()

  init {
//    var offset = 0
    var i = 0
    globalState.vertexArrayObject = vao

    for (attribute in attributes) {
      glVertexAttribPointer(i, attribute.size, GL_FLOAT, false, size, 0L)
      glEnableVertexAttribArray(i)
      i++
//      offset += attribute.size * 4
    }
  }

  fun activate(){
    globalState.vertexArrayObject = vao
  }
}