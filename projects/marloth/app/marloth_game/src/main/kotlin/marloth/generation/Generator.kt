package marloth.generation

import generation.calculateWorldScale
import generation.generateWorld
import mythic.ent.pipe
import randomly.Dice
import simulation.main.World
import simulation.main.addDeck
import simulation.misc.WorldBoundary
import simulation.misc.WorldInput
import simulation.misc.createWorldBoundary

fun generateDefaultWorld(): World {
  val input = WorldInput(
      boundary = createWorldBoundary(50f),
      dice = Dice(2)
  )
  throw Error("Not implemented")
//  val world = generateWorld(input)
//  return addEnemies(world, input.boundary, input.dice)
}

fun addEnemies(world: World, boundary: WorldBoundary, dice: Dice): World {
  val scale = calculateWorldScale(boundary.dimensions)
  return pipe(world, listOf(
      addDeck(placeCharacters(world.realm, dice, scale))
  ))
}
