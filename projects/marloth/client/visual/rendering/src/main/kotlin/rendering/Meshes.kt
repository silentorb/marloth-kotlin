package rendering

import mythic.drawing.DrawingVertexSchemas
import mythic.drawing.createDrawingVertexSchemas
import mythic.glowing.SimpleMesh
import mythic.glowing.VertexAttribute
import mythic.glowing.VertexSchema
import mythic.sculpting.*
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Vector3
import mythic.spatial.Vector4


data class NewMesh(val mesh: HalfEdgeMesh, val vertexSchema: VertexSchema)
typealias NewMeshMap = Map<String, NewMesh>

fun createCube(): FlexibleMesh {
  val mesh = FlexibleMesh()
//  create.squareDown(mesh, Vector2(1f, 1f), 1f)
  createCube(mesh, Vector3(1f, 1f, 1f))
  return mesh
}

fun createCylinder(): FlexibleMesh {
  val mesh = FlexibleMesh()
  createCylinder(mesh, 0.5f, 8, 1f)
  return mesh
}

fun createSphere(): FlexibleMesh {
  val mesh = FlexibleMesh()
  createSphere(mesh, 0.3f, 8, 6)
  return mesh
}

fun createHumanoid(): FlexibleMesh {
  val mesh = FlexibleMesh()
  val headPath = createArc(0.6f, 8, Pi).take(7)
  headPath.last().x *= 0.5f
//  val bodyPath = flipVertical(createArc(0.7f, 8, Pi).take(7)).reversed()
//  bodyPath.first().x *= 0.5f
//  translate(Matrix().scale(Vector3(1f, 1f, 1.2f)), bodyPath)

  val bodyFront = listOf(
      Vector3(0.05f, 0f, 1f),
      Vector3(0.1f, 0f, 0.75f),
      Vector3(0.5f, 0f, 0.7f),
      Vector3(0.5f, 0f, 0.25f),
      Vector3(0.5f, 0f, 0f)
  )

  val bodySide = listOf(
      Vector3(0.05f, 0f, 1f),
      Vector3(0.1f, 0f, 0.75f),
      Vector3(0.1f, 0f, 0.5f),
      Vector3(0.1f, 0f, 0.25f),
      Vector3(0.1f, 0f, 0f)
  )

  val neck = 0.05f
  val frontPath = joinPaths(neck, headPath, bodyFront)
  val sidePath = joinPaths(neck, headPath, bodySide)
  latheTwoPaths(mesh, frontPath, sidePath)
//  lathe(mesh, frontPath, 8)
  alignToFloor(mesh.distinctVertices)
  return mesh
}

data class VertexSchemas(
    val standard: VertexSchema,
    val textured: VertexSchema,
    val flat: VertexSchema,
    val drawing: DrawingVertexSchemas
)

fun createVertexSchemas() = VertexSchemas(
    standard = VertexSchema(listOf(
        VertexAttribute(0, "position", 3),
        VertexAttribute(1, "normal", 3),
        VertexAttribute(2, "color", 4)
    )),
    textured = VertexSchema(listOf(
        VertexAttribute(0, "position", 3),
        VertexAttribute(1, "normal", 3),
        VertexAttribute(2, "uv", 2)
    )),
    flat = VertexSchema(listOf(
        VertexAttribute(0, "position", 3)
    )),
    drawing = createDrawingVertexSchemas()
)

fun createSimpleMeshOld(mesh: HalfEdgeMesh, vertexSchema: VertexSchema) =
    convertMesh(mesh, vertexSchema, temporaryVertexSerializerOld)

fun createSimpleMesh(mesh: FlexibleMesh, vertexSchema: VertexSchema, color: Vector4) =
    convertMesh(mesh, vertexSchema, temporaryVertexSerializer(color))

fun createLineMesh(vertexSchema: VertexSchema) =
    SimpleMesh(vertexSchema, listOf(
        0f, 0f, 0f,
        1f, 0f, 0f
    ))

enum class MeshType {
  character,
  line,
  cylinder,
  sphere
}

typealias MeshGenerator = () -> FlexibleMesh

typealias MeshGeneratorMap = Map<MeshType, MeshGenerator>
typealias MeshMap = Map<MeshType, SimpleMesh>

fun createMeshes(vertexSchemas: VertexSchemas): MeshMap = mapOf(
    MeshType.character to createSimpleMesh(createHumanoid(), vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f)),
    MeshType.line to createLineMesh(vertexSchemas.flat),
    MeshType.cylinder to createSimpleMesh(createCylinder(), vertexSchemas.standard, Vector4(0.3f, 0.25f, 0.0f, 1f)),
    MeshType.sphere to createSimpleMesh(createSphere(), vertexSchemas.standard, Vector4(0.4f, 0.1f, 0.1f, 1f))
)