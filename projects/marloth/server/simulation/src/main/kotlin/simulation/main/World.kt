package simulation.main

import mythic.ent.Id
import mythic.ent.Table
import randomly.Dice
import simulation.misc.Character
import simulation.misc.GameOver
import simulation.misc.Player
import simulation.misc.Realm
import simulation.physics.Body

data class World(
    val realm: Realm,
    val nextId: Id,
    val deck: Deck,
    val dice: Dice,
    val availableIds: Set<Id>,
    val gameOver: GameOver? = null
) {
  val bodyTable: Table<Body> get() = deck.bodies
  val characterTable: Table<Character> get() = deck.characters

  val players: List<Player>
    get() = deck.players.values.toList()

  val characters: Collection<Character>
    get() = deck.characters.values

  val bodies: Collection<Body>
    get() = deck.bodies.values
}

typealias WorldTransform = (World) -> World

typealias WorldPair = Pair<World, World>
