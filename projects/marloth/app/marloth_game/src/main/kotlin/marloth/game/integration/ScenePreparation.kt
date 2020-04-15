package marloth.game.integration


//fun prepareSceneSdf(player: Id, world: World): RaySdf {
//  val deck = world.deck
//  val playerNode = deck.bodies[player]!!.node
//  val node = world.realm.nodeTable[playerNode]!!
//  val walls = world.realm.nodeTable[playerNode]!!.walls
//      .map { face ->
//        val wall = world.realm.mesh.faces[face]!!
//        boundedSphere(getCenter(wall.vertices), 1f)
//      }
////      .plusBounded(sphereSdf(Vector3(-58.23136f, 20.66085f, 1.4f), 1f))
//
//  val centers = world.realm.nodeTable[playerNode]!!.walls
//      .map { face ->
//        val wall = world.realm.mesh.faces[face]!!
//        getCenter(wall.vertices)
//      }

//  return plusBounded(walls)

//  return { ray ->
//    val localPlus = plusBounded(walls)(ray)
//    val k = 0
//    { hook, point ->
//      if (isInsideNode(point.xy(), node))
////    if (node.position.xy().distance(point.xy()) < node.radius + 10f)
//        localPlus(hook, point)
//      else
//        rayMiss
//    }
//  }
//val j = sphereSdf(Vector3(-40.23136f, 20.66085f, 1.4f), 1f)
//  return { j }
//}
