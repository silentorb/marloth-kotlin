package generation

import simulation.*

fun finalizeRealm(input: WorldInput, realm: Realm): World {
  val playerNode = realm.nodes.first()
  val scale = calculateWorldScale(input.boundary.dimensions)
  val nextId = newIdSource(1)
  val deck = Deck(
      factions = listOf(
          Faction(1, "Misfits"),
          Faction(2, "Monsters")
      )
  )
      .plus(toDeck(newPlayer(nextId, playerNode)))
      .plus(placeWallLamps(realm, nextId, input.dice, scale))
//  instantiator.newPlayer(1)

//  placeWallLamps(world, instantiator, input.dice, scale)

  return World(
      deck = deck,
      nextId = nextId(),
      realm = realm)
}