package marloth.generation.population

import simulation.misc.floorOffset
import marloth.scenery.enums.MeshInfoMap
import marloth.definition.templates.newBuffCloud
import marloth.definition.templates.newTreasureChest
import silentorb.mythic.ent.Id
import marloth.scenery.enums.MeshId
import simulation.accessorize.AccessoryName
import simulation.main.Deck
import simulation.misc.Node

fun groupElementsByCell(deck: Deck, elements: Collection<Id>): Map<Id, Set<Id>> {
  return elements
      .groupBy { id ->
        val body = deck.bodies[id]!!
        body.nearestNode
      }
      .mapValues { it.value.toSet() }
}

//fun placeBuffCloud(node: Node, buff: AccessoryName) =
//    newBuffCloud(
//        position = nodeFloorCenter(node),
//        radius = node.radius,
//        buff = buff
//    )

//fun placeTreasureChest(meshInfo: MeshInfoMap, node: Node, amount: Int) =
//    newTreasureChest(meshInfo, alignWithNodeFloor(meshInfo, node, MeshId.treasureChest) + floorOffset, amount)
