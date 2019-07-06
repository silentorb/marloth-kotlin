import mythic.sculpting.ImmutableEdgeReference
import mythic.sculpting.ImmutableEdge
import mythic.sculpting.ImmutableFace
import mythic.spatial.Vector3
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import org.jetbrains.spek.api.dsl.on
import simulation.physics.MovingBody
import simulation.physics.checkWallCollision
import simulation.physics.getWallCollisions
import simulation.main.simulationDelta
import mythic.ent.IdSource
import mythic.ent.newIdSource
import org.junit.jupiter.api.Assertions.assertTrue
import simulation.physics.WallCollision3

class SimulationSpec : Spek({
  describe("simulation logic") {

    on("bodies collide with room corners") {

      fun newFace(nextId: IdSource, edges: List<Pair<Vector3, Vector3>>, normal: Vector3) =
          ImmutableFace(0,
              edges
                  .map {
                    ImmutableEdgeReference(
                        edge = ImmutableEdge(nextId(), it.first, it.second, mutableListOf()),
                        direction = true
                    )
                  }.toMutableList(),
              normal = normal
          )

      xit("should smoothly settle in the corner") {
        val delta = simulationDelta
        val nextId = newIdSource(0)
        val wallsInRange = listOf(
            newFace(nextId, listOf(
                Pair(Vector3(0f, 0f, 0f), Vector3(4f, 0f, 0f))
            ), Vector3(0f, -1f, 0f)),
            newFace(nextId, listOf(
                Pair(Vector3(0f, 0f, 0f), Vector3(0f, -4f, 0f))
            ), Vector3(1f, 0f, 0f))
        )

        val positions = mutableListOf(Vector3(0.52f, -0.54f, 0f))
        val offset = Vector3(-1f, 1f, 0f).normalize() * delta

        for (i in 0..30) {
          val body = MovingBody(
              radius = 0.5f,
              position = positions.last()
          )
          val walls = getWallCollisions(body, offset, wallsInRange)
          val newPosition = checkWallCollision(body, offset, walls)
          positions.add(newPosition)
        }

        val target = Vector3(0.5f, -0.5f, 0f)
        val position = positions.last()
        val distances = positions.map { it.distance(target) }
        assertTrue(position.roughlyEquals(target))
      }

      xit("Can round a corner without getting too close") {
        val delta = simulationDelta
        val cornerPoint = Vector3(1f, 0f, 0f)
        val nextId = newIdSource(0)
        val wallsInRange = listOf(
            newFace(nextId, listOf(
                Pair(Vector3(0f, 0f, 0f), cornerPoint)
            ), Vector3(0f, -1f, 0f))
        )

        val positions = mutableListOf(Vector3(0.985f, -0.52f, 0f))
        val offset = Vector3(0.3f, 1f, 0f).normalize() * 2f * delta

        val radius = 0.5f
        for (i in 0..30) {
          val body = MovingBody(
              radius = radius,
              position = positions.last()
          )
          val walls = getWallCollisions(body, offset, wallsInRange)
          val newPosition = checkWallCollision(body, offset, walls)
          positions.add(newPosition)
        }

        val distances = positions.map {
          val distance = it.distance(cornerPoint)
          if (distance < 0.5f)
            distance - 0.5f
          else
            0f
        }
        val position = positions.last()

        assertTrue(position.y < 0.5f)
        assertTrue(distances.none { it < 0f })
      }

      it("Can round a corner without getting stuck") {
        val delta = simulationDelta
        val cornerPoint = Vector3(1f, 0f, 0f)
        val nextId = newIdSource(0)
        val wallsInRange = listOf(
            newFace(nextId, listOf(
                Pair(Vector3(0f, 0f, 0f), cornerPoint)
            ), Vector3(0f, -1f, 0f)),
            newFace(nextId, listOf(
                Pair(cornerPoint, Vector3(2f, 1f, 0f))
            ), Vector3(1f, -1f, 0f).normalize())
        )

//        val start = Vector3(0.99f, -0.5f, 0f)
//        val start = Vector3(1.002771f, -0.5f, 0.0f)\
        val start = Vector3(1.4889201f, -0.23607628f, 0.0f)
        val positions = mutableListOf(start)
        val offset = Vector3(0.3f, 1f, 0f).normalize() * 4f * delta
        val collisions: MutableList<List<WallCollision3>> = mutableListOf()

        val radius = 0.5f
        for (i in 0..60) {
          val body = MovingBody(
              radius = radius,
              position = positions.last()
          )
          val walls = getWallCollisions(body, offset, wallsInRange)
          val newPosition = checkWallCollision(body, offset, walls)
          collisions.add(walls)
          positions.add(newPosition)
        }
        data class All(
            val position: Vector3,
            val offset: Vector3,
            val length: Float,
            val dot: Float,
            val distance: Float
        )

        val position = positions.last()
        val lengths = positions.dropLast(1).zip(positions.drop(1)) { a, b ->
          a.distance(b)
        }
        val offsets = positions.dropLast(1).zip(positions.drop(1)) { a, b ->
          b - a
        }
        val dots = offsets.dropLast(1).zip(offsets.drop(1)) { a, b ->
          a.normalize().dot(b.normalize())
        }
        val distances = positions.map { it.distance(cornerPoint) }

        val all = dots.mapIndexed { i, dot ->
          All(
              length = lengths[i],
              offset = offsets[i],
              dot = dot,
              position = positions[i],
              distance = distances[i]
          )
        }
        // Ensure the shifts in direction are subtle.
        assertTrue(dots.all { it > 0.9f })

        // Ensure the corner was rounded
        assertTrue(position.y > -0.45f)
      }
    }
  }
})
