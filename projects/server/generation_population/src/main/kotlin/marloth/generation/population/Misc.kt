package marloth.generation.population

import simulation.misc.floorOffset
import generation.architecture.misc.MeshInfoMap
import generation.architecture.old.alignWithNodeFloor
import generation.architecture.old.nodeFloorCenter
import marloth.definition.templates.newBuffCloud
import marloth.definition.templates.newTreasureChest
import silentorb.mythic.ent.Id
import marloth.definition.enums.MeshId
import marloth.scenery.enums.ModifierId
import simulation.main.Deck
import simulation.misc.MapGrid
import simulation.misc.Node
import simulation.misc.CellAttribute

fun groupElementsByCell(deck: Deck, elements: Collection<Id>): Map<Id, Set<Id>> {
  return elements
      .groupBy { id ->
        val body = deck.bodies[id]!!
        body.nearestNode
      }
      .mapValues { it.value.toSet() }
}

fun placeBuffCloud(node: Node, buff: ModifierId) =
    newBuffCloud(
        position = nodeFloorCenter(node),
        radius = node.radius,
        buff = buff
    )

fun placeTreasureChest(meshInfo: MeshInfoMap, node: Node, amount: Int) =
    newTreasureChest(meshInfo, alignWithNodeFloor(meshInfo, node, MeshId.treasureChest.name) + floorOffset, amount)

fun getPlayerCell(grid: MapGrid) =
    grid.cells.entries.first { it.value.attributes.contains(CellAttribute.home) }.key
