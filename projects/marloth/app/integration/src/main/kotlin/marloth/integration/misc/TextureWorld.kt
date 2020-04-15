package marloth.integration.misc

//fun createTexturedHorizontalSurface(face: ImmutableFace, texture: TextureId): TextureFace {
//  val vertices = face.unorderedVertices
//  val bounds = getBounds(vertices)
//  val scale = 2f
//  val offset = Vector2(
//      (bounds.start.x / scale).roundToInt().toFloat() * scale,
//      (bounds.start.y / scale).roundToInt().toFloat() * scale
//  )
//  val scaleX = 1f / scale
//  val scaleY = 1f / scale
//  return TextureFace(face.id, vertices.associate { vertex ->
//    Pair(vertex, VertexNormalTexture(
//        Vector3(0f, 0f, 1f),
//        Vector2(
//            (vertex.x - offset.x) * scaleX,
//            (vertex.y - offset.y) * scaleY
//        )
//    ))
//  },
//      texture
//  )
//}

//fun createTexturedWall(face: ImmutableFace, texture: TextureId): TextureFace {
//  val vertices = face.unorderedVertices
//  val bounds = getBounds(vertices)
//  val dimensions = bounds.dimensions
//  val scale = .5f
//  val ceilingEdge = getCeiling(face).edge
//  val length = ceilingEdge.first.distance(ceilingEdge.second) * scale
//  val uvs = listOf(
//      Vector2(0f, 0f),
//      Vector2(length, 0f),
//      Vector2(length, 2f),
//      Vector2(0f, 2f)
//  )
//  val floorEdge = getFloor(face).edge
//  val isFirstEdgeHorizontal = ceilingEdge.matches(vertices[0], vertices[1])
//      || floorEdge.matches(vertices[0], vertices[1])
//
//  val alignedUvs = if (isFirstEdgeHorizontal)
//    uvs
//  else
//    listOf(uvs.last()).plus(uvs.dropLast(1))
//
//  val uvIterator = alignedUvs.listIterator()
//  if (face.id == 392L) {
//    val k = 0
//  }
//  return TextureFace(face.id, vertices.associate { vertex ->
//    Pair(vertex, VertexNormalTexture(
//        Vector3(0f, 0f, 1f),
//        uvIterator.next()
//    ))
//  },
//      texture
//  )
//}

//fun prepareWorldMesh(realm: Realm, node: Node, textures: TextureLibrary): List<TextureFace> {
////  val floorTexture = if (node.type == NodeType.space) textures[Textures.darkCheckers]!! else textures[Textures.checkers]!!
//  val floors = node.floors.map { Pair(realm.faces[it]!!, it) }
//      .filter { (it, _) -> it.firstNode == node.id && it.texture != null }
//
//  val ceilings = node.ceilings.map { Pair(realm.faces[it]!!, it) }
//      .filter { (it, _) -> it.firstNode == node.id && it.texture != null }
//
//  return floors.plus(ceilings)
//      .map { createTexturedHorizontalSurface(realm.mesh.faces[it.second]!!, it.first.texture!!) }
////      .plus(
////          node.walls.map { Pair(realm.faces[it]!!, it) }
////              .filter { (it, _) -> it.firstNode == node.id && it.texture != null }
////              .map { createTexturedWall(realm.mesh.faces[it.second]!!, it.first.texture!!) }
////      )
//}

//fun convertSectorMesh(realm: Realm, faces2: ImmutableFaceTable, renderer: Renderer, node: Node): SectorMesh {
//  val texturedFaces = prepareWorldMesh(realm, node, renderer.mappedTextures)
//  val vertexInfo = texturedFaces.associate { Pair(it.face, it.vertexMap) }
//  val serializer = texturedVertexSerializer(vertexInfo)
//  return SectorMesh(
//      id = node.id,
//      mesh = convertMesh(texturedFaces.map { faces2[it.face]!! }, renderer.vertexSchemas.textured, serializer),
//      textureIndex = texturedFaces.map { it.texture }
//  )
//}
//
//fun convertWorldMesh(realm: Realm, renderer: Renderer): WorldMesh {
//  val faces2 = realm.mesh.faces.values.associate { Pair(it.id, it) }
//  val sectors = realm.nodeList.map {
//    convertSectorMesh(realm, faces2, renderer, it)
//  }
//  return WorldMesh(sectors)
//}

//fun setWorldMesh(realm: Realm, client: Client) {
//  client.renderer.worldMesh = convertWorldMesh(realm, client.renderer)
//}