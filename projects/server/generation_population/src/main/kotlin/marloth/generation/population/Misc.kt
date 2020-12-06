package marloth.generation.population

//fun groupElementsByCell(deck: Deck, elements: Collection<Id>): Map<Id, Set<Id>> {
//  return elements
//      .groupBy { id ->
//        val body = deck.bodies[id]!!
//        body.nearestNode
//      }
//      .mapValues { it.value.toSet() }
//}

//fun placeBuffCloud(node: Node, buff: AccessoryName) =
//    newBuffCloud(
//        position = nodeFloorCenter(node),
//        radius = node.radius,
//        buff = buff
//    )

//fun placeTreasureChest(meshInfo: MeshInfoMap, node: Node, amount: Int) =
//    newTreasureChest(meshInfo, alignWithNodeFloor(meshInfo, node, MeshId.treasureChest) + floorOffset, amount)
