package simulation.misc

import silentorb.mythic.ent.Table
import silentorb.mythic.spatial.interpolate
import simulation.main.World
import simulation.updating.simulationDelta

fun <T> interpolateTables(scalar: Float, first: Table<T>, second: Table<T>, action: (Float, T, T) -> T): Table<T> =
    first.keys.plus(second.keys).associateWith { key ->
      val a = first[key]
      val b = second[key]
      if (a != null && b != null)
        action(scalar, a, b)
      else
        a ?: b!!
    }
//    first.mapValues { (key, a) ->
//      val b = second[key]
//      if (b == null)
//        a
//      else
//        action(scalar, a, b)
//    }

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

  return second.copy(
      deck = second.deck.copy(
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

