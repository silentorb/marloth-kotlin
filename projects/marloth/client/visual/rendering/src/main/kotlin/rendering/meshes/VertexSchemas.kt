package rendering.meshes

import mythic.drawing.DrawingVertexSchemas
import mythic.drawing.createDrawingVertexSchemas
import mythic.glowing.VertexAttribute

typealias VertexSchema = mythic.glowing.VertexSchema<AttributeName>

data class VertexSchemas(
    val imported: VertexSchema,
    val textured: VertexSchema,
    val flat: VertexSchema,
    val animated: VertexSchema,
    val shaded: VertexSchema,
    val drawing: DrawingVertexSchemas
)

fun createVertexSchemas() = VertexSchemas(
    shaded = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3),
        VertexAttribute(AttributeName.normal, 3)
    )),
    textured = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3),
        VertexAttribute(AttributeName.normal, 3),
        VertexAttribute(AttributeName.uv, 2)
    )),
    animated = VertexSchema(listOf(
        VertexAttribute(AttributeName.position, 3),
        VertexAttribute(AttributeName.normal, 3),
        VertexAttribute(AttributeName.uv, 2),
        VertexAttribute(AttributeName.joints, 4),
        VertexAttribute(AttributeName.weights, 4)
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
