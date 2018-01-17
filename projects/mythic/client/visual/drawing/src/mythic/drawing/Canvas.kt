package mythic.drawing

import mythic.glowing.*
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import org.joml.Vector2i
import org.joml.Vector4i
import org.joml.minus
import kotlin.math.cos
import kotlin.math.sin

data class Meshes(
    val square: SimpleMesh,
    val image: SimpleMesh,
    val circle: SimpleMesh,
    val solidCircle: SimpleMesh
)

data class DrawingVertexSchemas(
    val simple: VertexSchema,
    val image: VertexSchema
)

fun createDrawingVertexSchemas() = DrawingVertexSchemas(
    VertexSchema(listOf(VertexAttribute(0, "position", 2))),
    VertexSchema(listOf(VertexAttribute(0, "vertex", 4)))
)

fun createSquareMesh(vertexSchema: VertexSchema) =
    SimpleMesh(vertexSchema, listOf(
        0f, 1f,
        0f, 0f,
        1f, 0f,
        1f, 1f
    ))

fun createImageMesh(vertexSchema: VertexSchema) =
    SimpleMesh(vertexSchema, listOf(
        0f, 1f, 0f, 1f,
        0f, 0f, 0f, 0f,
        1f, 0f, 1f, 0f,
        1f, 1f, 1f, 1f
    ))

fun createCircleList(radius: Float, count: Int): ArrayList<Float> {
  val vertices = ArrayList<Float>((count) * 2)
  val increment = Pi * 2 / count

  for (i in 0..count) {
    val theta = increment * i
    vertices.add(sin(theta) * radius)
    vertices.add(cos(theta) * radius)
  }
  return vertices
}

fun createCircleMesh(vertexSchema: VertexSchema, radius: Float, count: Int) =
    SimpleMesh(vertexSchema, createCircleList(radius, count))

fun createSolidCircleMesh(vertexSchema: VertexSchema, radius: Float, count: Int) =
    SimpleMesh(vertexSchema, listOf(0f, 0f).plus(createCircleList(radius, count)))

private val circleResolution = 32

fun createDrawingMeshes(vertexSchemas: DrawingVertexSchemas) = Meshes(
    square = createSquareMesh(vertexSchemas.simple),
    image = createImageMesh(vertexSchemas.image),
    circle = createCircleMesh(vertexSchemas.simple, 1f, circleResolution),
    solidCircle = createSolidCircleMesh(vertexSchemas.simple, 1f, circleResolution)
)

enum class FillType {
  solid,
  outline
}

typealias Brush = (Matrix, Drawable) -> Unit

class Canvas(
    val vertexSchemas: DrawingVertexSchemas,
    val meshes: Meshes,
    val effects: DrawingEffects,
    val unitScaling: Vector2,
    dimensions: Vector2i
) {

  val dynamicMesh = MutableSimpleMesh(vertexSchemas.simple)
  val viewportDimensions = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
  val pixelsToScalar = Matrix().scale(1f / dimensions.x, 1f / dimensions.y, 1f)

  fun transformScalar(position: Vector2, dimensions: Vector2) =
      Matrix()
          .mul(pixelsToScalar)
          .translate(position.x, position.y, 0f)
          .scale(dimensions.x, dimensions.y, 1f)

  fun drawSquare(position: Vector2, dimensions: Vector2, brush: Brush) {
    brush(transformScalar(position, dimensions), meshes.square)
  }

  fun drawCircle(position: Vector2, radius: Float, brush: Brush) {
    brush(transformScalar(position, Vector2(radius, radius)), meshes.circle)
  }

  fun drawSolidCircle(position: Vector2, radius: Float, brush: Brush) {
    brush(transformScalar(position, Vector2(radius, radius)), meshes.solidCircle)
  }

  fun draw(color: Vector4, drawMethod: DrawMethod, transform: Matrix, mesh: Drawable) {
    effects.singleColorShader.activate(transform, color)
    mesh.draw(drawMethod)
  }

  fun outline(color: Vector4, thickness: Float): Brush = { transform: Matrix, mesh: Drawable ->
    globalState.lineThickness = thickness
    draw(color, DrawMethod.lineLoop, transform, mesh)
  }

  fun solid(color: Vector4) = { transform: Matrix, mesh: Drawable ->
    draw(color, DrawMethod.triangleFan, transform, mesh)
  }

  fun image(texture: Texture) = { transform: Matrix, mesh: Drawable ->
    effects.image.activate(transform, texture)
    mesh.draw(DrawMethod.triangleFan)
  }

  fun drawImage(position: Vector2, dimensions: Vector2, brush: Brush) {
    brush(transformScalar(position, dimensions), meshes.image)
  }

  fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float, color: Vector4, thickness: Float) {
    dynamicMesh.load(listOf(startX, startY, endX, endY))
    outline(color, thickness)(Matrix().mul(pixelsToScalar), dynamicMesh)
  }

  fun drawLine(start: Vector2, end: Vector2, color: Vector4, thickness: Float) {
    drawLine(start.x, start.y, end.x, end.y, color, thickness)
  }

  fun drawText(config: TextConfiguration) {
    drawTextRaw(config, effects.coloredImage, vertexSchemas.image, pixelsToScalar)
  }

  fun crop(value: Vector4i, action: () -> Unit) = cropStack(value, action)
}
