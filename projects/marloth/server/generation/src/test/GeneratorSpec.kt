import generation.abstract.getOverlapping
import generation.abstract.intersects
import generation.abstract.isBetween
import generation.createTestWorld
import generation.overlaps2D
import generation.structure.generateStructure
import junit.framework.TestCase.*
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.projectPointOntoLine
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import simulation.*

class GeneratorSpec : Spek({
  describe("world generation") {

    on("testing overlapping") {

      val first = Node(Vector3(0f, 0f, 0f), 5f, NodeType.room)
      val second = Node(Vector3(8f, 0f, 0f), 5f, NodeType.room)
      val third = Node(Vector3(200f, 0f, 0f), 15f, NodeType.room)

      it("should detect overlap") {
        val overlaps = overlaps2D(first, second)
        assertEquals(true, overlaps)
      }

      it("should detect no overlap") {
        val overlaps = overlaps2D(second, third)
        assertEquals(false, overlaps)
      }

      it("should return a single pair") {
        val matches = getOverlapping(listOf(first, second, third))
        assertEquals(1, matches.size)
        assertSame(first, matches[0].first)
        assertSame(second, matches[0].second)
      }
    }

    on("intersection between a line and a circle") {

      it("should detect middle") {
        assertTrue(isBetween(0f, 10f, 5f))
        assertFalse(isBetween(0f, 10f, 11f))
        assertTrue(isBetween(10f, -5f, 0f))
        assertFalse(isBetween(10f, -5f, -150f))
      }

      it("sees that a line does not intersect a circle") {
        val result = intersects(
            Vector2(-27.357315f, 35.34888f),
            Vector2(-31.243145f, 3.1939812f),
            Vector2(-33.03736f, -7.435528f), 8.65439f)

        assertFalse(result)
      }
    }

    on("skinning two connected nodes") {
      val world = createTestWorld().meta

//      assertEquals(world.mesh.edges.size, v2)
    }
  }

  describe("spatial calculations") {

    it("can project a 2D point onto a circle") {
//      val result = projectPointOntoLine(Vector2(1f, 1f), Vector2(2f, 0f))
//      assertEquals(result, Vector2(1f,0f))
    }
  }
})