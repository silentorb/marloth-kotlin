package mythic.glowing

class VertexAttribute<T>(
    val name: T,
    val size: Int
)


class VertexAttributeDetail<T>(
    val id: Int,
    val name: T,
    val offset: Int,
    val size: Int
)