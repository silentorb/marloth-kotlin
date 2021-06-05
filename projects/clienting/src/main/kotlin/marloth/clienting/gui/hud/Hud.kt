package marloth.clienting.gui.hud

import marloth.clienting.ClientState
import marloth.clienting.gui.Colors
import marloth.clienting.gui.DeviceMode
import marloth.clienting.gui.TextResources
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.actionItemText
import marloth.clienting.gui.menus.black
import marloth.clienting.gui.menus.general.verticalList
import silentorb.mythic.bloom.*
import silentorb.mythic.characters.rigs.ViewMode
import silentorb.mythic.characters.rigs.isGrounded
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugOverrides
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.gpuProfileMeasurements
import simulation.accessorize.Accessory
import simulation.characters.Character
import simulation.combat.general.ResourceContainer
import simulation.entities.DoorMode
import simulation.entities.Interactable
import simulation.entities.Interactions
import simulation.entities.getInteractionCommandType
import simulation.happenings.Notification
import simulation.main.World
import simulation.misc.Definitions
import kotlin.math.roundToInt

private val textStyle = TextStyles.smallGray

data class Cooldown(
    val name: String,
    val value: Float
)

fun floatToRoundedString(value: Float): String =
    ((value * 100).roundToInt().toFloat() / 100).toString()

fun resourceString(value: Int, max: Int): String {
  return "$value / $max"
}

fun resourceString(resource: ResourceContainer): String {
  val value = resource.value
  val max = resource.max
  return "$value / $max"
}

fun reverseResourceString(resource: ResourceContainer): String {
  val max = resource.max
  val value = max - resource.value
  return "$value / $max"
}

val df = java.text.DecimalFormat("#0.00")

fun notificationFlower(notification: Notification): Box {
  val from = notification.from
  return boxMargin(10)(
      verticalList(listOfNotNull(
          label(TextStyles.smallWhite, notification.message),
          if (from != null) label(TextStyles.smallGray, from) else null,
      ))
  ) depictBehind solidBackground(Colors.darkGray)
}

fun notificationsFlower(notifications: List<Notification>): Flower =
    alignBoth(justifiedEnd, centered,
        hudBox(
            boxList(verticalPlane, 10)(
                notifications
                    .sortedBy { it.elapsedTime }
                    .map(::notificationFlower)
            )
        )
    )

private var gpuTimeLast: Long = 0L
private var gpuTimeHold: Int = 0

fun getGpuTime(): Long =
    if (gpuTimeHold > 0) {
      --gpuTimeHold
      gpuTimeLast
    } else {
      gpuTimeHold = 6
      gpuTimeLast = gpuProfileMeasurements["all"]?.last ?: 0
      gpuTimeLast
    }

fun getUtilityItemText(definitions: Definitions, accessories: Map<Id, Accessory>, character: Character): String? {
  val item = character.utilityItem
  return if (item == null)
    null
  else {
    val accessory = accessories[item]
    val accessoryDefinition = definitions.accessories[accessory?.type]
    if (accessoryDefinition == null)
      null
    else
      actionItemText(definitions, accessoryDefinition, accessory!!.quantity)
  }
}

fun playerStats(world: World, actor: Id, debugInfo: List<String>, accessories: Map<Id, Accessory>): Flower {
  val deck = world.deck
  val destructible = deck.destructibles[actor]!!
  val character = deck.characters[actor]!!
  val accessoryPoints = character.accessoryPoints + if (character.accessoryOptions != null) 1 else 0
  val equipment = deck.accessories.filter { it.value.owner == actor }.values
  val definitions = world.definitions
  val utilityItemText = getUtilityItemText(definitions, accessories, character)
  val rows = listOfNotNull(
      label(textStyle, "Health: ${resourceString(destructible.health, destructible.maxHealth)}"),
//      label(textStyle, "Nourishment: ${highPercentage(character.nourishment)}"),
      label(textStyle, "Energy: ${character.energy}"),
//      label(textStyle, "Health AC: ${destructible.healthAccumulator}"),
//      label(textStyle, "Energy AC: ${character.energyAccumulator}"),
//      if (getDebugBoolean("HUD_DRAW_RAW_NOURISHMENT")) label(textStyle, "RawNourish: ${character.nourishment}") else null,
//      label(textStyle, "Doom: ${world.global.doom}")
//      label(textStyle, "Sanity: ${resourceString(data.madness)}")
  ) + listOfNotNull(
      if (accessoryPoints > 0) label(textStyle, "Ability Points: $accessoryPoints") else null,
      if (utilityItemText != null) label(textStyle, utilityItemText) else null,
  ) + debugInfo.map {
    label(textStyle, it)
  } + if (getDebugBoolean("HUD_DRAW_INVENTORY")) equipment.mapNotNull { accessory ->
    val definition = definitions.accessories[accessory.type]
    if (definition == null)
      null
    else
      label(textStyle, definitions.textLibrary(definition.name))
  }
  else
    listOf()

  return alignBoth(justifiedStart, justifiedEnd,
      hudBox(
          boxList(verticalPlane, 10)(rows)
      )
  )
}

fun getInteractKeyText(deviceMode: DeviceMode) =
    when (deviceMode) {
      DeviceMode.gamepad -> "(A)"
      DeviceMode.mouseKeyboard -> "E"
    }

fun getInteractableLabelText(interactable: Interactable, mode: String?): String? =
    when (getInteractionCommandType(interactable.type, mode)) {
      Interactions.sleep -> "Sleep"
      Interactions.take -> "Take"
      Interactions.open -> "Open"
      Interactions.close -> "Close"
      else -> null
    }

fun interactionDialog(primaryInteractText: String, textResources: TextResources, interactable: Interactable,
                      mode: String?): Flower? {
  val label = getInteractableLabelText(interactable, mode)
  return if (label == null)
    null
  else {
    val rows = listOf(
        label(textStyle, "$label $primaryInteractText"),
    )

    alignSingle(percentage(0.8f), verticalPlane,
        alignSingle(centered, horizontalPlane,
            boxMargin(10)(boxList(verticalPlane, 20)(rows))
        ) depictBehind solidBackground(black)
    )
  }
}

fun hudLayout(textResources: TextResources, world: World, clientState: ClientState, player: Id,
              debugInfo: List<String>, view: ViewId?): Flower? {
  val deck = world.deck
  val definitions = world.definitions
  val guiState = clientState.guiStates[player]
  val notifications = guiState?.notifications ?: listOf()
  val character = deck.characters[player]
  val characterRig = deck.characterRigs[player]
  return if (character == null || guiState == null)
    null
  else {
    val body = deck.bodies[player]!!

    val accessories = deck.accessories
        .filter { it.value.owner == player }

    val interactable = if (view == null && character.isAlive)
      deck.interactables[character.canInteractWith]
    else null


    val cooldowns = accessories
        .mapNotNull { (accessory, accessoryRecord) ->
          val cooldown = deck.actions[accessory]?.cooldown
          if (cooldown != null && cooldown != 0f) {
            val accessoryDefinition = definitions.accessories[accessoryRecord.type]
            val definitionCooldown = definitions.actions[accessoryRecord.type]?.cooldown
            if (accessoryDefinition != null && definitionCooldown != null)
              Cooldown(
                  name = textResources(accessoryDefinition.name)!!,
                  value = 1f - cooldown / definitionCooldown,
              )
            else
              null
          } else
            null
        }
        .plus(
            accessories
                .mapNotNull { (id, accessory) ->
                  val timer = deck.timersFloat[id]
                  if (timer != null) {
                    val definition = definitions.accessories[accessory.type]
                    if (definition != null)
                      Cooldown(
                          name = textResources(definition.name)!!,
                          value = 1f - timer.duration / timer.original
                      )
                    else
                      null
                  } else
                    null
                }
        )

    val viewMode = characterRig?.viewMode ?: ViewMode.firstPerson
    val debugInfo = listOfNotNull(
//            "LR: ${floatToRoundedString(lightRating(deck, player))}",
        if (getDebugBoolean("HUD_DRAW_PLAYER_VELOCITY")) "Velocity: ${floatToRoundedString(body.velocity.length())}" else null,
        if (getDebugBoolean("HUD_DRAW_CELL_LOCATION"))
          "${body.position.x.toInt() / 5} ${body.position.y.toInt() / 5} ${body.position.z.toInt() / 5}"
        else
          null,
        if (getDebugBoolean("HUD_DRAW_PLAYER_ROTATION"))
          "${characterRig?.facingRotation?.x} ${characterRig?.facingRotation?.y}"
        else
          null,
//            floatToRoundedString(deck.thirdPersonRigs[player]!!.rotation.x)
//            deck.characterRigs[player]!!.hoverCamera!!.pitch.toString()
//        if (getDebugString() != "") getDebugString() else null,
        if (getDebugBoolean("HUD_DRAW_LOCATION"))
          "${floatToRoundedString(body.position.x)} ${floatToRoundedString(body.position.y)} ${floatToRoundedString(body.position.z)}"
        else
          null,
        if (getDebugBoolean("HUD_DRAW_MOUSE_LOCATION"))
          clientState.input.deviceStates.first().mousePosition.toString()
        else
          null,
        if (characterRig != null && getDebugBoolean("HUD_DRAW_GROUNDED"))
          if (isGrounded(characterRig)) "Grounded" else "Air"
        else
          null,
        if (getDebugBoolean("HUD_DRAW_GROUND_DISTANCE"))
          characterRig?.groundDistance?.toString() ?: "?"
        else
          null,
        if (getDebugBoolean("HUD_DRAW_GPU_RENDER_TIME")) {
          val time = getGpuTime()
          "GPU: " + String.format("%,d", time).padStart(14, ' ')
        } else
          null,
    ) + debugInfo +
        getDebugOverrides()
            .map { (key, value) -> "$key: $value" }

    compose(listOfNotNull(
        if (interactable != null)
          interactionDialog(getInteractKeyText(guiState.primarydeviceMode), textResources, interactable, deck.primaryModes[character.canInteractWith]?.mode)
        else
          null,
        playerStats(world, player, debugInfo, accessories),
        if (notifications.any()) notificationsFlower(notifications) else null,
        if (cooldowns.any()) cooldownIndicatorPlacement(cooldowns) else null,
        if (viewMode == ViewMode.firstPerson && character.isAlive) reticlePlacement() else null,
    ))
  }
}
