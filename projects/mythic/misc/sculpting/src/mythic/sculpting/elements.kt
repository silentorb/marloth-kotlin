package mythic.sculpting

import mythic.spatial.Vector2
import mythic.spatial.Vector3

class Vertex(
    var position: Vector3,
    var edge: Edge? = null
)

class Edge(
    var vertex: Vertex,
    var next: Edge? = null,
    var previous: Edge? = null,
    var opposite: Edge? = null,
    var face: Face? = null
)

class Face(
    var edge: Edge?
)

data class VertexNormalTexture(
    var normal: Vector3,
    var uv: Vector2
)