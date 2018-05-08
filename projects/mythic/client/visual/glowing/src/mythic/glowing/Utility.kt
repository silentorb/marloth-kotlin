package mythic.glowing

import org.lwjgl.opengl.GL11.*

fun getErrorInfo(error: Int): String {
  when (error) {
    GL_INVALID_OPERATION ->
      return "GL_INVALID_OPERATION"

    GL_INVALID_VALUE ->
      return "GL_INVALID_VALUE"

    GL_INVALID_ENUM ->
      return "GL_INVALID_ENUM"
  }

  return error.toString()
}

fun checkError(message: String) {
  val error = glGetError()
  if (error != GL_NO_ERROR) {
    val info = getErrorInfo(error)
    throw Error("OpenGL Error " + info + " while " + message)
  }
}
