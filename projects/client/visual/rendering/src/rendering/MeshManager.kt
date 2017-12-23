package rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.glutils.ShaderProgram

class MeshManager {
  private val meshes: MeshMap
  constructor() {
    val vertexSchema = arrayOf(
        VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
        VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
        VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
    )
    val newMeshes = createMeshes(vertexSchema)
    meshes = newMeshes.mapValues({ (_, m) -> convertMesh(m.mesh, m.vertexSchema) })
    Gdx.gl.glClearColor(0f, 0f, 0.5f, 1f)
//    camera = PerspectiveCamera()
//    camera.translate(0f, -10f, 0f)
  }
}