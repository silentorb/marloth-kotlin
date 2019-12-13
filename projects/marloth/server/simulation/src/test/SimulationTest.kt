import silentorb.mythic.spatial.Vector3
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import simulation.entities.getMovementImpulseVector

fun assertRoughEquals(first: Float, second: Float, buffer: Float): Boolean {
  return second > first - buffer && second < first + buffer
}

class SimulationTest {

  @Test
  fun acceleration() {
    val baseSpeed = 1f
    val velocity = Vector3(0f, 1f, 0f)
    val noVelocity = Vector3.zero
    val a = getMovementImpulseVector(baseSpeed, noVelocity, Vector3(0f, 1f, 0f)).length()
    val a2 = getMovementImpulseVector(baseSpeed, velocity, Vector3(0f, 1f, 0f)).length()
    val b = getMovementImpulseVector(baseSpeed, velocity, Vector3(1f, 1f, 0f).normalize()).length()
    val c = getMovementImpulseVector(baseSpeed, velocity, Vector3(1f, 0f, 0f).normalize()).length()
//    assertTrue(assertRoughEquals(a, b, 0.1f))
    assertTrue(assertRoughEquals(a, c, 0.1f))
  }
}
