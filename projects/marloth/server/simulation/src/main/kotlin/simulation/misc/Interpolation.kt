package simulation.misc

import mythic.ent.Table
import mythic.spatial.interpolate
import simulation.main.World
import simulation.main.simulationDelta

fun <T> interpolateTables(scalar: Float, first: Table<T>, second: Table<T>, action: (Float, T, T) -> T): Table<T> =
    first.mapValues { (key, a) ->
      val b = second[key]
      if (b == null)
        a
      else
        action(scalar, a, b)
    }

fun interpolateWorlds(accumulator: Double, first: World, second: World): World? {
  val scalar = (accumulator / simulationDelta).toFloat()
  val bodies = interpolateTables(scalar, first.deck.bodies, second.deck.bodies) { s, body, next ->
    body.copy(
        position = interpolate(s, body.position, next.position),
        orientation = interpolate(s, body.orientation, next.orientation)
    )
  }
  val characters = interpolateTables(scalar, first.deck.characters, second.deck.characters) { s, a, b ->
    a.copy(
        facingRotation = interpolate(s, a.facingRotation, b.facingRotation)
    )
  }

  return first.copy(
      deck = first.deck.copy(
          bodies = bodies,
          characters = characters
      )
  )
}

fun interpolateWorlds(accumulator: Double, worlds: List<World>): World? =
    if (worlds.size < 2)
      worlds.firstOrNull()
    else
      interpolateWorlds(accumulator, worlds[0], worlds[1])

