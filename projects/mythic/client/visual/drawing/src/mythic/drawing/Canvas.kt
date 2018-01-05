package mythic.drawing

import mythic.glowing.*
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import org.joml.Vector2i
import kotlin.math.cos
import kotlin.math.sin

data class Meshes(
    val square: SimpleMesh,
    val circle: SimpleMesh
)

data class DrawingVertexSchemas(
    val simple: VertexSchema,
    val coloredImage: VertexSchema
)

fun createDrawingVertexSchemas() = DrawingVertexSchemas(
    VertexSchema(listOf(VertexAttribute(0, "position", 2))),
    VertexSchema(listOf(
        VertexAttribute(0, "vertex", 4)
    ))
)

fun createSquareMesh(vertexSchema: VertexSchema) =
    SimpleMesh(vertexSchema, listOf(
        0f, 1f,
        0f, 0f,
        1f, 0f,
        1f, 1f
    ))


fun createCircleMesh(vertexSchema: VertexSchema, radius: Float, count: Int): SimpleMesh {
  val vertices = ArrayList<Float>((count + 1) * 2)
  vertices.add(0f)
  vertices.add(0f)
  val increment = Pi * 2 / count

  for (i in 0..count) {
    val theta = increment * i
    vertices.add(sin(theta) * radius)
    vertices.add(cos(theta) * radius)
  }
  return SimpleMesh(vertexSchema, vertices)
}

fun createDrawingMeshes(vertexSchemas: DrawingVertexSchemas) = Meshes(
    createSquareMesh(vertexSchemas.simple),
    createCircleMesh(vertexSchemas.simple, 1f, 8)
)

enum class FillType {
  solid,
  outline
}

class Canvas(
    val vertexSchemas: DrawingVertexSchemas,
    val meshes: Meshes,
    val effects: DrawingEffects,
    val unitScaling: Vector2,
    dimensions: Vector2i
) {

  val viewportDimensions = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
  val pixelsToScalar = Matrix().scale(1f / dimensions.x, 1f / dimensions.y, 1f)

  fun transformScalar(position: Vector2, dimensions: Vector2) =
      Matrix()
          .mul(pixelsToScalar)
          .translate(position.x, position.y, 0f)
          .scale(dimensions.x, dimensions.y, 1f)

  //  private fun drawMesh(position: Vector2, dimensions: Vector2, mesh: SimpleMesh, color: Vector4, drawMethod: DrawMethod) {
  fun drawMesh(position: Vector2, dimensions: Vector2, mesh: SimpleMesh, draw: (transform: Matrix, mesh: SimpleMesh) -> Unit) =
      draw(transformScalar(position, dimensions), mesh)
//    effects.singleColorShader.activate(transform, color)
//    mesh.draw(drawMethod)

  fun draw(color: Vector4, drawMethod: DrawMethod) = { transform: Matrix, mesh: SimpleMesh ->
    effects.singleColorShader.activate(transform, color)
    mesh.draw(drawMethod)
  }

  fun drawLine(color: Vector4, thickness: Float) = {
    globalState.lineThickness = thickness
    draw(color, DrawMethod.lineLoop)
  }

  fun drawSolid(color: Vector4) = { draw(color, DrawMethod.triangleFan) }

  fun drawSolidSquare(position: Vector2, dimensions: Vector2, color: Vector4) {
    drawMesh(position, dimensions, color, meshes.square, DrawMethod.triangleFan)
  }

  fun drawLineSquare(position: Vector2, dimensions: Vector2, color: Vector4, thickness: Float) {
    globalState.lineThickness = thickness
    drawMesh(position, dimensions, color, meshes.square, DrawMethod.lineLoop)
  }

  fun drawLineCircle(position: Vector2, dimensions: Vector2, color: Vector4, thickness: Float) {
    globalState.lineThickness = thickness
    drawMesh(position, dimensions, color, meshes.square, DrawMethod.lineLoop)
  }

  fun drawText(config: TextConfiguration) {
    drawTextRaw(config, effects.coloredImage, vertexSchemas.coloredImage, pixelsToScalar)
  }
}