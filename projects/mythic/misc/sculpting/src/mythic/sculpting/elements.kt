package mythic.sculpting

import mythic.spatial.Vector3

class Vertex(
    var position: Vector3,
    edge: Edge? = null
) {
  private var _edge: Edge? = edge
  var edge: Edge
    get() = _edge!!
    set(value) {
      _edge = value
    }
}

class Edge(
    var vertex: Vertex,
    var face: Face,
    next: Edge? = null,
    opposite: Edge? = null
) {
  var next: Edge = next ?: this
  var opposite: Edge = opposite ?: this
}

class Face(
    edge: Edge? = null
) {
  private var _edge: Edge? = edge
  var edge: Edge
    get() = _edge!!
    set(value) {
      _edge = value
    }
}
