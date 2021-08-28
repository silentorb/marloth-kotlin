package marloth.clienting.editing

import generation.architecture.biomes.Biomes
import generation.general.*
import imgui.ImGui
import marloth.definition.misc.blockSides
import silentorb.mythic.editing.components.*
import silentorb.mythic.editing.main.*
import silentorb.mythic.ent.Serialization
import silentorb.mythic.ent.reflectProperties
import silentorb.mythic.spatial.Vector3i
import simulation.entities.DepictionType
import simulation.entities.InteractionActions
import simulation.entities.depictionTypes
import simulation.misc.BlockRotations
import simulation.misc.GameProperties
import simulation.misc.modeTypes

val cellDirectionWidget: PropertyWidget = { _, entry, owner ->
  val value = entry.target as? CellDirection ?: stagingValue(owner) { CellDirection(Vector3i.zero, Direction.east) }
  val cell = value.cell
  ImGui.text("X")
  ImGui.sameLine()
  ImGui.setNextItemWidth(40f)
  val x = integerTextField("${owner}.x", cell.x)
  ImGui.sameLine()
  ImGui.text("Y")
  ImGui.sameLine()
  ImGui.setNextItemWidth(40f)
  val y = integerTextField("${owner}.y", cell.y)
  ImGui.sameLine()
  ImGui.text("Z")
  ImGui.sameLine()
  ImGui.setNextItemWidth(40f)
  val z = integerTextField("${owner}.z", cell.z)
  ImGui.sameLine()
  ImGui.text("Dir")
  ImGui.sameLine()
  ImGui.setNextItemWidth(80f)
  val direction = dropDownWidget(directionNames, "$owner.dir", value.direction.name)
  if (direction == null || (x == value.cell.x && y == value.cell.y && z == value.cell.z && direction == value.direction.name))
    null
  else
    CellDirection(Vector3i(x, y, z), Direction.valueOf(direction))
}

val cellDirectionSerialization = Serialization(
    load = {
      val value = it as? List<Any>
      if (value == null || value.size != 4)
        throw Error("Invalid CellDirection syntax")
      else
        CellDirection(
            cell = Vector3i(value[0] as Int, value[1] as Int, value[2] as Int),
            direction = Direction.valueOf(value[3] as String)
        )
    },
    save = {
      val cellDirection = it as? CellDirection
      if (cellDirection == null)
        listOf()
      else
        listOf(it.cell.x, it.cell.y, it.cell.z, cellDirection.direction.name)
    }
)

val biomeIds = reflectProperties<String>(Biomes)
val blockRotationOptions = BlockRotations.values().associate { it to it.name }
val blockRotationsWidget: PropertyWidget = labeledDropDownWidget { blockRotationOptions }
val interactionIds = reflectProperties<String>(InteractionActions)

fun marlothEditorPropertyDefinitions(sides: List<String> = blockSides): PropertyDefinitions = mapOf(
    GameProperties.mine to PropertyDefinition(
        displayName = "My Type",
        widget = dropDownWidget { sides },
        defaultValue = { sides.firstOrNull() },
    ),
    GameProperties.other to PropertyDefinition(
        displayName = "Other Type",
        widget = dropDownWidget { sides },
        defaultValue = { sides.firstOrNull() },
    ),
    GameProperties.direction to PropertyDefinition(
        displayName = "Direction",
        serialization = cellDirectionSerialization,
        widget = cellDirectionWidget,
        defaultValue = { CellDirection(Vector3i.zero, Direction.east) },
    ),
    GameProperties.showIfSideIsEmpty to PropertyDefinition(
        displayName = "Show if Side is Empty",
        serialization = cellDirectionSerialization,
        widget = cellDirectionWidget,
        defaultValue = { CellDirection(Vector3i.zero, Direction.east) },
    ),
    GameProperties.hideIfSideIsEmpty to PropertyDefinition(
        displayName = "Hide if Side is Empty",
        serialization = cellDirectionSerialization,
        widget = cellDirectionWidget,
        defaultValue = { CellDirection(Vector3i.zero, Direction.east) },
    ),
    GameProperties.sideHeight to PropertyDefinition(
        displayName = "Side Height",
        serialization = intSerialization,
        widget = propertyIntegerTextField,
        defaultValue = { StandardHeights.first },
    ),
    GameProperties.heightVariant to PropertyDefinition(
        displayName = "Height Variant",
        serialization = intSerialization,
        widget = propertyIntegerTextField,
        defaultValue = { 25 },
    ),
    GameProperties.biome to PropertyDefinition(
        displayName = "Biome",
        widget = dropDownWidget { biomeIds },
        defaultValue = { Biomes.checkers },
    ),
    GameProperties.blockRotations to PropertyDefinition(
        displayName = "Block Rotations",
        widget = blockRotationsWidget,
        serialization = enumSerialization { BlockRotations.valueOf(it) },
        defaultValue = { BlockRotations.all },
    ),
    GameProperties.interaction to PropertyDefinition(
        displayName = "Interaction",
        widget = dropDownWidget { interactionIds },
        defaultValue = { InteractionActions.take },
    ),
    GameProperties.onInteract to PropertyDefinition(
        displayName = "On Interact",
        widget = propertyTextField,
        defaultValue = { "" },
    ),
    GameProperties.itemType to PropertyDefinition(
        displayName = "Item Type",
        widget = propertyTextField,
        defaultValue = { "" },
    ),
    GameProperties.modeType to PropertyDefinition(
        displayName = "Mode Type",
        widget = dropDownWidget { modeTypes },
        defaultValue = { modeTypes.first() },
    ),
    GameProperties.mode to PropertyDefinition(
        displayName = "Mode",
        widget = propertyTextField,
        defaultValue = { modeTypes.first() },
    ),
    GameProperties.mass to PropertyDefinition(
        displayName = "Mass",
        serialization = floatSerialization,
        widget = propertyDecimalTextField,
        defaultValue = { 20f },
    ),
    GameProperties.rarity to PropertyDefinition(
        displayName = "Rarity",
        widget = dropDownWidget { Rarity.values().indices.map { it + 1 } },
        defaultValue = { 1 },
    ),
    GameProperties.depiction to PropertyDefinition(
        displayName = "Depiction",
        widget = dropDownWidget { depictionTypes },
        defaultValue = { DepictionType.staticMesh },
    ),
) + commonPropertyDefinitions()
