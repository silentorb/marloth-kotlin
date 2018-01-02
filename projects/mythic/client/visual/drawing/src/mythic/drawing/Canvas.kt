package mythic.drawing

import mythic.glowing.*
import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import org.joml.Vector2i
import org.joml.times

data class Meshes(
    val square: SimpleMesh
)

data class DrawingVertexSchemas(
    val simpleSquare: VertexSchema,
    val coloredImage: VertexSchema
)

fun createDrawingVertexSchemas() = DrawingVertexSchemas(
    VertexSchema(listOf(VertexAttribute(0, "position", 2))),
    VertexSchema(listOf(
        VertexAttribute(0, "vertex", 4)
    ))
)

fun createSquareMesh(vertexSchema: VertexSchema): SimpleMesh {
  return SimpleMesh(vertexSchema, listOf(
      0f, 1f,
      0f, 0f,
      1f, 0f,
      1f, 1f
  ))
}

fun createDrawingMeshes(vertexSchemas: DrawingVertexSchemas) = Meshes(
    createSquareMesh(vertexSchemas.simpleSquare)
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

  val dimensions = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
  val pixelsToScalar = Matrix().scale(1f / dimensions.x, 1f / dimensions.y, 1f)

  private fun drawSquare(position: Vector2, dimensions: Vector2, color: Vector4, drawMethod: DrawMethod) {
    val transform = Matrix()
        .mul(pixelsToScalar)
        .translate(position.x, position.y, 0f)
        .scale(dimensions.x, dimensions.y, 1f)

    effects.singleColorShader.activate(transform, color)
    meshes.square.draw(drawMethod)
  }

  fun drawSolidSquare(position: Vector2, dimensions: Vector2, color: Vector4) {
    drawSquare(position, dimensions, color, DrawMethod.triangleFan)
  }

  fun drawLineSquare(position: Vector2, dimensions: Vector2, color: Vector4, thickness: Float) {
    globalState.lineThickness = thickness
    drawSquare(position, dimensions, color, DrawMethod.lineLoop)
  }

  fun drawText(config: TextConfiguration) {
    drawTextRaw(config, effects.coloredImage, vertexSchemas.coloredImage, pixelsToScalar)
  }
}