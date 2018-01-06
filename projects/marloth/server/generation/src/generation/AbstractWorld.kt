package generation

class AbstractWorld {
  val nodes: MutableList<Node> = mutableListOf()
  val connections: MutableList<Connection> = mutableListOf()

  fun connect(first: Node, second: Node): Connection {
    val connection = Connection(first, second)
    connections.add(connection)
    first.connections.add(connection)
    second.connections.add(connection)
    return connection
  }
}