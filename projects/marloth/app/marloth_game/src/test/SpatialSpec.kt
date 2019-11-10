import mythic.spatial.*
import org.joml.times
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse

//class SpatialSpec : Spek({
//  beforeGroup {
//    System.setProperty("joml.format", "false")
//  }
//
//  describe("spatial functions") {
//
//    on("getRotationMatrix") {
//
//      it("should return proper rotation") {
//        val transform = Matrix()
//            .rotateZ(Pi / 4)
//            .translate(1f, 1f, 0f)
//            .rotateZ(Pi / 4)
//
//        val original = Vector3m(1f, 0f, 0f)
//        val rotationTransform = getRotationMatrix(transform)
//        val point1 = original.transform(rotationTransform)
////        assertSame(Vector3m(0f, 1f, 0f), point1)
//      }
//    }
//
//    on("applying a quat to a vector") {
//
//      it("should rotate properly") {
//        val point1 = Quaternion().rotateZ(Pi / 2) * Vector3m(1f, 1f, 0f)
//        assertEquals(Vector3m(-1f, 1f, 0f), point1)
//      }
//    }
//
//    on("checking if a point is inside a polygon") {
//
//      it("should detect outside points") {
//        val result = isInsidePolygon(Vector2(-0.742f, -0.075f), listOf(
//            Vector2(-0.025f, -0.001f),
//            Vector2(-0.025f, -0.14f),
//            Vector2(-0.078f, -0.138f),
//            Vector2(-0.092f, 0.003f)
//        ))
//
//        assertFalse(result)
//      }
//    }
//
//  }
//
//})
