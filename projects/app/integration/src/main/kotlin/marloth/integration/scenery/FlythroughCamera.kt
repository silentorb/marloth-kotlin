package marloth.integration.scenery

import marloth.clienting.ClientState
import marloth.clienting.rendering.defaultAngle
import marloth.integration.misc.mapGameCommands
import silentorb.mythic.characters.rigs.*
import silentorb.mythic.haft.GAMEPAD_AXIS_TRIGGER_LEFT
import silentorb.mythic.haft.GAMEPAD_AXIS_TRIGGER_RIGHT
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.scenery.Camera
import silentorb.mythic.scenery.ProjectionType
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3
import simulation.updating.simulationDelta

data class FlyThroughCameraState(
    var location: Vector3,
    var rig: CharacterRig
)

private var flyThroughCameraState: FlyThroughCameraState? = null

fun defaultFlyThroughState() =
    FlyThroughCameraState(
        location = Vector3.zero,
        rig = CharacterRig(facingOrientation = Quaternion.zero)
    )

fun getFlyThroughCameraState(initialize: () -> FlyThroughCameraState = ::defaultFlyThroughState): FlyThroughCameraState {
  if (flyThroughCameraState == null) {
    flyThroughCameraState = initialize()
  }

  return flyThroughCameraState!!
}

fun newFlyThroughCamera(location: Vector3, orientation: Quaternion): Camera =
    Camera(
        ProjectionType.perspective,
        location,
        orientation,
        defaultAngle
    )

fun newFlyThroughCamera(initialize: () -> FlyThroughCameraState): Camera {
  val (location, rig) = getFlyThroughCameraState(initialize)
  return newFlyThroughCamera(location, rig.facingOrientation)
}

fun flyThroughOrientation(rig: CharacterRig, commands: List<CharacterCommand>): Vector2 {
  val firstPersonLookVelocity = updateLookVelocityFirstPerson(commands, defaultGamepadMomentumAxis(), rig.firstPersonLookVelocity)
  return updateFirstPersonFacingRotation(rig.facingRotation, null, firstPersonLookVelocity * 20f, simulationDelta)
}

fun updateFlyThroughCamera(clientState: ClientState) {
  if (flyThroughCameraState == null)
    return

  val state = getFlyThroughCameraState()
  val (_, rig) = state
  val commands = mapGameCommands(clientState.players, clientState.commands)
  val facingRotation = flyThroughOrientation(rig, commands)
  val movement = characterMovement(commands, rig, null, 0L)
  if (movement != null) {
    state.location = state.location + movement.offset * 12f * simulationDelta
  }
  val deviceEvents = clientState.input.deviceStates.flatMap { it.events }
  val zSpeed = 15f
  val zOffset = if (deviceEvents.any { it.index == GAMEPAD_AXIS_TRIGGER_LEFT }) {
    Vector3(0f, 0f, -zSpeed * simulationDelta)
  } else if (deviceEvents.any { it.index == GAMEPAD_AXIS_TRIGGER_RIGHT }) {
    Vector3(0f, 0f, zSpeed* simulationDelta)
  } else
    Vector3.zero

  state.location = state.location + zOffset

  state.rig = state.rig.copy(
      facingRotation = facingRotation,
      facingOrientation = characterRigOrentation(facingRotation)
  )
}
