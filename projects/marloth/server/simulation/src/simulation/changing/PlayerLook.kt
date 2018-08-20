package simulation.changing

import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus
import simulation.Character
import simulation.CommandType
import simulation.Commands
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

val firstPersonLookMap = mapOf(
    CommandType.lookLeft to Vector3(0f, 0f, 1f),
    CommandType.lookRight to Vector3(0f, 0f, -1f),
    CommandType.lookUp to Vector3(0f, -1f, 0f),
    CommandType.lookDown to Vector3(0f, 1f, 0f)
)

val cameraLookMap = mapOf(
    CommandType.cameraLookLeft to Vector3(0f, 0f, 1f),
    CommandType.cameraLookRight to Vector3(0f, 0f, -1f),
    CommandType.cameraLookUp to Vector3(0f, 1f, 0f),
    CommandType.cameraLookDown to Vector3(0f, -1f, 0f)
)

fun applyLookForce(lookMap: Map<CommandType, Vector3>, character: Character, commands: Commands): Vector2 {
  val offset3 = joinInputVector(commands, lookMap)
  return if (offset3 != null) {
    val offset2 = Vector2(offset3.z, offset3.y)
    offset2 * mouseLookSensitivity * character.turnSpeed
  } else
    Vector2()
}

fun characterLookForce(character: Character, commands: Commands): Vector2 =
    applyLookForce(firstPersonLookMap, character, commands)

//
//fun playerRotateTP(player: Player, commands: Commands): Vector2? =
//    characterLookForce(playerLookMapTP, player, commands)

//fun applyPlayerLookCommands(commands: Commands, delta: Float): Vector2? {
//  return characterLookForce(firstPersonLookMap, commands)
////  if (player.viewMode == ViewMode.firstPerson) {
////    playerRotateFP(player, commands)
////  } else if (player.viewMode == ViewMode.thirdPerson) {
////    playerRotateTP(player, commands)
////  }
//}

//fun updatePlayerLookForce(player: Player, commands: Commands): Vector2? {
//  return when (player.viewMode) {
//    ViewMode.firstPerson -> playerRotateFP(player, commands)
//    player.viewMode -> playerRotateTP(player, commands)
//    else -> throw Error("Not supported")
//  }
//}

const val maxLookVelocityChange = 0.1f

fun updatePlayerLookVelocity(lookForce: Vector2, lookVelocity: Vector2): Vector2 {
  val diff = lookForce - lookVelocity
  val diffLength = diff.length()
  return if (diffLength != 0f) {
    val adjustment = if (diffLength > maxLookVelocityChange) diff.normalize() * maxLookVelocityChange else diff
    lookVelocity + adjustment
  } else
    lookVelocity
}

fun fpCameraRotation(velocity: Vector2, delta: Float): Vector3 {
  val deltaVelocity = velocity * delta
  return if (velocity.y != 0f || velocity.x != 0f) {
    Vector3(0f, deltaVelocity.y, deltaVelocity.x)
  } else
    Vector3()
}

fun updateTpCameraRotation(player: Player, character: Character, delta: Float): Vector3? {
  val velocity = character.lookVelocity
  val deltaVelocity = velocity * delta
  return if (velocity.y != 0f || velocity.x != 0f) {
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
