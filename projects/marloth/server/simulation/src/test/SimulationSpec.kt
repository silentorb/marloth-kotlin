import mythic.sculpting.EdgeReference
import mythic.sculpting.FlexibleEdge
import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3
import mythic.spatial.Vector3m
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import physics.MovingBody
import physics.checkWallCollision
import physics.getWallCollisions
import simulation.FaceInfo
import simulation.FaceType
import simulation.simulationDelta
import junit.framework.TestCase.*


class SimulationSpec : Spek({
  describe("simulation logic") {

    on("bodies collide with room corners") {

      fun newFace(edges: List<Pair<Vector3m, Vector3m>>, normal: Vector3) =
          FlexibleFace(
              edges
                  .map {
                    EdgeReference(
                        edge = FlexibleEdge(it.first, it.second, mutableListOf()),
                        next = null,
                        previous = null,
                        direction = true
                    )
                  }.toMutableList(),
              normal = normal
          )
      it("should smoothly settle in the corner") {
        val delta = simulationDelta
        val wallsInRange = listOf(
            newFace(listOf(
                Pair(Vector3m(0f, 0f, 0f), Vector3m(4f, 0f, 0f))
            ), Vector3(0f, -1f, 0f)),
            newFace(listOf(
                Pair(Vector3m(0f, 0f, 0f), Vector3m(0f, -4f, 0f))
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

      it("Can round a corner without getting too close") {
        val delta = simulationDelta
        val cornerPoint = Vector3(1f, 0f, 0f)
        val wallsInRange = listOf(
            newFace(listOf(
                Pair(Vector3m(0f, 0f, 0f), Vector3m(cornerPoint))
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
    }
  }
})