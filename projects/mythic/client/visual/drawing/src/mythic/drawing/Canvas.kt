package mythic.drawing

import mythic.glowing.DrawMethod
import mythic.glowing.SimpleMesh
import mythic.glowing.VertexAttribute
import mythic.glowing.VertexSchema
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration

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
    val unitScaling: Vector2) {

  fun drawSquare(position: Vector2, dimensions: Vector2, fillType: FillType = FillType.solid) {
    val drawMethod =
        if (fillType == FillType.solid)
          DrawMethod.triangleFan
        else DrawMethod.lines

    meshes.square.draw(drawMethod)
  }

  fun drawColoredSquare(position: Vector2, dimensions: Vector2, color: Vector4, fillType: FillType = FillType.solid) {

  }

  fun drawText(config: TextConfiguration) {
    drawTextRaw(config, effects.coloredImage, vertexSchemas.coloredImage, unitScaling)
  }
}