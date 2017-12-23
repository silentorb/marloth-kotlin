package sculpting

import spatial.*

data class Vertex(
    var position: Vector3,
    var edge: Edge? = null
)

class Edge(
    var vertex: Vertex? = null,
    var face: Face? = null,
    var next: Edge? = null,
    var opposite: Edge? = null
)

data class Face(
    var edge: Edge? = null
)

//interface Vertex {
//  var position: sculpting.Vector3
//  var edge: Edge
//}

//interface Half_Edge: Edge {
//  var vertex: Vertex
//  var next: Edge
//override  var opposite: Edge
//  var face: Face
//}
//
//interface Edge {
//  var vertex: Vertex
//  var next: Edge
//  var opposite: Edge?
//  var face: Face
//}
//
//interface Face {
//  var edge: Edge
//}
