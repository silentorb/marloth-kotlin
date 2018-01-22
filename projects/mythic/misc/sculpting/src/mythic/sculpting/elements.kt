package mythic.sculpting

import mythic.spatial.Vector2
import mythic.spatial.Vector3

class HalfEdgeVertex(
    var position: Vector3,
    var edge: HalfEdge? = null
)

class HalfEdge(
    var vertex: HalfEdgeVertex,
    var next: HalfEdge? = null,
    var previous: HalfEdge? = null,
    var opposite: HalfEdge? = null,
    var face: HalfEdgeFace? = null
)

class HalfEdgeFace(
    var edge: HalfEdge?
)

data class VertexNormalTexture(
    var normal: Vector3,
    var uv: Vector2
)