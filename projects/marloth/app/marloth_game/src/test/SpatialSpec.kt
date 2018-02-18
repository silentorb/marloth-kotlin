import junit.framework.TestCase.*
import mythic.spatial.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

class SpatialSpec : Spek({
  beforeGroup {
    System.setProperty("joml.format", "false")
  }

  describe("spatial functions") {

    on("getRotationMatrix") {

      it("should return proper rotation") {
        val transform = Matrix()
            .rotateZ(Pi / 4)
            .translate(1f, 1f, 0f)
            .rotateZ(Pi / 4)

        val original = Vector3(1f, 0f, 0f)
        val rotationTransform = getRotationMatrix(transform)
        val point1 = original.transform(rotationTransform)
        assertEquals(Vector3(0f, 1f, 0f), point1)
      }

    }

  }

})