import generation.Node
import generation.getOverlapping
import generation.overlaps2D
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertSame
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
  }
})