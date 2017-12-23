package glowing

import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

class ShaderProgram(val vertexShader: VertexShader, val fragmentShader: FragmentShader) {
  var programId: Int

  init {
    programId = glCreateProgram()
    glAttachShader(programId, vertexShader.id)
    glAttachShader(programId, fragmentShader.id)
    glBindAttribLocation(programId, 0, "position")
    glBindFragDataLocation(programId, 0, "color")
    glLinkProgram(programId)
    val linked = glGetProgrami(programId, GL_LINK_STATUS)
    val programLog = glGetProgramInfoLog(programId)
    if (programLog!!.trim({ it <= ' ' }).isNotEmpty())
      throw Error(programLog)

    if (linked == 0)
      throw Error("Could not link program.")
  }

  constructor(vertexCode: String, fragmentCode: String) :
      this(VertexShader(vertexCode), FragmentShader(fragmentCode)) {

  }
}