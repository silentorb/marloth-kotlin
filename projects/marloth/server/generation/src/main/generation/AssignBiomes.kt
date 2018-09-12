package generation

import generation.abstract.Realm
import randomly.Dice
import simulation.Biome
import simulation.Id

typealias BiomeMap = Map<Id, Biome>

fun assignBiomes(realm: Realm, biomes: List<Biome>, dice: Dice): BiomeMap {
//  biome = dice.getItem(biomes),
  return realm.nodes.associate { node ->
    val biome = dice.getItem(biomes)
    Pair(node.id, biome)
  }
}