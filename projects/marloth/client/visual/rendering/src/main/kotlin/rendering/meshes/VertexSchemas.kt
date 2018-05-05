package rendering.meshes

import mythic.drawing.DrawingVertexSchemas
import mythic.drawing.createDrawingVertexSchemas
import mythic.glowing.VertexAttribute

typealias VertexSchema = mythic.glowing.VertexSchema<AttributeName>

data class VertexSchemas(
    val standard: VertexSchema,
    val imported: VertexSchema,
    val textured: VertexSchema,
    val flat: VertexSchema,
    val drawing: DrawingVertexSchemas
)

fun createVertexSchemas() = VertexSchemas(
    standard = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3),
        VertexAttribute(AttributeName.normal, 3),
        VertexAttribute(AttributeName.color, 4)
    )),
    textured = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3),
        VertexAttribute(AttributeName.normal, 3),
        VertexAttribute(AttributeName.uv, 2)
    )),
    flat = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3)
    )),
    imported = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3),
        VertexAttribute(AttributeName.normal, 3)
    )),
    drawing = createDrawingVertexSchemas()
)
