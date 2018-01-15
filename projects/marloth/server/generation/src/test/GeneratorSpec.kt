import generation.*
import generation.abstract.Node
import generation.abstract.intersects
import generation.abstract.isBetween
import junit.framework.TestCase.*
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

class GeneratorSpec : Spek({
  describe("world generation") {

    on("testing overlapping") {

      val first = Node(Vector3(0f, 0f, 0f), 5f)
      val second = Node(Vector3(8f, 0f, 0f), 5f)
      val third = Node(Vector3(200f, 0f, 0f), 15f)

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
  }
})