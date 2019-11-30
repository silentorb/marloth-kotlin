package marloth.generation.population

//fun newDoor(realm: Realm, nextId: IdSource): (Id) -> Hand = { nodeId ->
//  val node = realm.nodeTable[nodeId]!!
//  val j = node.walls.map { realm.faces[it]!! }
//  val face = node.walls.first { realm.faces[it]!!.faceType != FaceType.wall }
//  val meshFace = realm.mesh.faces[face]!!
//  val edge = getFloor(meshFace)
//  val margin = 0.9f
//  val length = edge.first.distance(edge.second) * margin
//  val position = getCenter(meshFace.vertices)
//  val id = nextId()
//  val sideEdge = realm.mesh.faces[face]!!.edges.filter(isVerticalEdgeLimited).first()
//  val height = Math.abs(sideEdge.first.z - sideEdge.second.z)
//  EntityTemplates.door.copy(
//      id = id,
//      body = Body(
//          id = id,
//          position = position,
//          orientation = Quaternion().rotateTo(Vector3(0f, 1f, 0f), realm.mesh.faces[face]!!.normal),
//          scale = Vector3(length, 1f, height * margin),
//          node = nodeId
//      )
//  )
//}

//fun placeDoors(realm: Realm, nextId: IdSource): Deck =
//    toDeck(
//        realm.doorFrameNodes.map(newDoor(realm, nextId))
//    )
