package simulation.changing

import commanding.CommandType
import haft.Commands
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus
import simulation.Player
import simulation.ViewMode

data class MomentumConfig(
    val attack: Float,
    val sustain: Float,
    val release: Float
)

data class MomentumConfig2(
    val yaw: MomentumConfig,
    val pitch: MomentumConfig
)

operator fun MomentumConfig.times(value: Float): MomentumConfig =
    MomentumConfig(attack * value, sustain * value, release * value)

operator fun MomentumConfig2.times(value: Float): MomentumConfig2 =
    MomentumConfig2(yaw * value, pitch * value)

operator fun MomentumConfig2.times(value: Vector2): MomentumConfig2 =
    MomentumConfig2(yaw * value.x, pitch * value.y)

private val mouseLookSensitivity = Vector2(1f, 1f)
private val gamepadLookSensitivity = Vector2(1f, 1f)

private val firstPersonLookMomentum = MomentumConfig2(
    MomentumConfig(1.7f, 4f, 1f),
    MomentumConfig(1f, 4f, 1f)
)

private val thirdPersonLookMomentum = MomentumConfig2(
    MomentumConfig(1.7f, 4f, 1f),
    MomentumConfig(1f, 4f, 1f)
)

//private val firstPersonLookMomentum = MomentumConfig2(
//    MomentumConfig(3f, 4f),
//    MomentumConfig(1f, 4f)
//)

val playerLookMapFP = mapOf(
    CommandType.lookLeft to Vector3(0f, 0f, 1f),
    CommandType.lookRight to Vector3(0f, 0f, -1f),
    CommandType.lookUp to Vector3(0f, -1f, 0f),
    CommandType.lookDown to Vector3(0f, 1f, 0f)
)

val playerLookMapTP = mapOf(
    CommandType.lookLeft to Vector3(0f, 0f, 1f),
    CommandType.lookRight to Vector3(0f, 0f, -1f),
    CommandType.lookUp to Vector3(0f, 1f, 0f),
    CommandType.lookDown to Vector3(0f, -1f, 0f)
)

fun playerRotate(lookMap: Map<CommandType, Vector3>, player: Player, commands: Commands<CommandType>): Vector2? {
  val speed = 1f
  val offset3 = joinInputVector(commands, lookMap)
  return if (offset3 != null) {
    val offset2 = Vector2(offset3.z, offset3.y)
    offset2 * mouseLookSensitivity * speed
  } else
    null
}

fun playerRotateFP(player: Player, commands: Commands<CommandType>): Vector2? =
    playerRotate(playerLookMapFP, player, commands)


fun playerRotateTP(player: Player, commands: Commands<CommandType>): Vector2? =
    playerRotate(playerLookMapTP, player, commands)

fun applyPlayerLookCommands(player: Player, commands: Commands<CommandType>, delta: Float) {
  if (player.viewMode == ViewMode.firstPerson) {
    playerRotateFP(player, commands)
  } else if (player.viewMode == ViewMode.thirdPerson) {
    playerRotateTP(player, commands)
  }
}

fun updatePlayerLookForce(player: Player, commands: Commands<CommandType>): Vector2? {
  return when (player.viewMode) {
    ViewMode.firstPerson -> playerRotateFP(player, commands)
    player.viewMode -> playerRotateTP(player, commands)
    else -> throw Error("Not supported")
  }
}

fun updatePlayerLookVelocity(lookForce: Vector2, lookVelocity: Vector2): Vector2 {
  val diff = lookForce - lookVelocity
  val diffLength = diff.length()
  val maxLength = 0.05f
  return if (diffLength != 0f) {
    val adjustment = if (diffLength > maxLength) diff.normalize() * maxLength else diff
    lookVelocity + adjustment
  } else
    lookVelocity
}

fun updatePlayerRotation(player: Player, delta: Float): Vector3? {
  val velocity = player.lookVelocity
  val deltaVelocity = velocity * delta
  return if (velocity.y != 0f || velocity.x != 0f) {
    val m = (if (player.viewMode == ViewMode.firstPerson)
      firstPersonLookMomentum
    else
      thirdPersonLookMomentum) * mouseLookSensitivity

    if (player.viewMode == ViewMode.firstPerson)
      Vector3(0f, deltaVelocity.y, deltaVelocity.x)
    else {
      val hoverCamera = player.hoverCamera
      hoverCamera.pitch += deltaVelocity.y
      hoverCamera.yaw += deltaVelocity.y
      val hoverPitchMin = -1.0f // Up
      val hoverPitchMax = 0.0f // Down

      if (hoverCamera.pitch > hoverPitchMax)
        hoverCamera.pitch = hoverPitchMax

      if (hoverCamera.pitch < hoverPitchMin)
        hoverCamera.pitch = hoverPitchMin

      null
//      println("p " + hoverCamera.pitch + ", y" + hoverCamera.yaw + " |  vp " + player.lookVelocity.y + ",vy " + player.lookVelocity.z)
    }
  } else
    null
}
