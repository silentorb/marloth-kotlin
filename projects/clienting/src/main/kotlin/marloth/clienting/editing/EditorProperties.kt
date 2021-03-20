package marloth.clienting.editing

import generation.architecture.biomes.Biomes
import generation.general.CellDirection
import generation.general.Direction
import generation.general.StandardHeights
import generation.general.directionNames
import imgui.ImGui
import marloth.definition.misc.blockSides
import silentorb.mythic.editing.*
import silentorb.mythic.editing.components.*
import silentorb.mythic.ent.Serialization
import silentorb.mythic.ent.reflectProperties
import silentorb.mythic.spatial.Vector3i
import simulation.misc.BlockRotations
import simulation.misc.MarlothProperties

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
val blockRotationOptions =  BlockRotations.values().associate { it to it.name }
val blockRotationsWidget: PropertyWidget = labeledDropDownWidget { blockRotationOptions }

fun marlothEditorPropertyDefinitions(sides: List<String> = blockSides): PropertyDefinitions = mapOf(
    MarlothProperties.mine to PropertyDefinition(
        displayName = "My Type",
        widget = dropDownWidget { sides },
        defaultValue = { sides.firstOrNull() },
    ),
    MarlothProperties.other to PropertyDefinition(
        displayName = "Other Type",
        widget = dropDownWidget { sides },
        defaultValue = { sides.firstOrNull() },
    ),
    MarlothProperties.direction to PropertyDefinition(
        displayName = "Direction",
        serialization = cellDirectionSerialization,
        widget = cellDirectionWidget,
        defaultValue = { CellDirection(Vector3i.zero, Direction.east) },
    ),
    MarlothProperties.showIfSideIsEmpty to PropertyDefinition(
        displayName = "Show if Side is Empty",
        serialization = cellDirectionSerialization,
        widget = cellDirectionWidget,
        defaultValue = { CellDirection(Vector3i.zero, Direction.east) },
    ),
    MarlothProperties.sideHeight to PropertyDefinition(
        displayName = "Side Height",
        serialization = intSerialization,
        widget = propertyIntegerTextField,
        defaultValue = { StandardHeights.first },
    ),
    MarlothProperties.heightVariant to PropertyDefinition(
        displayName = "Height Variant",
        serialization = intSerialization,
        widget = propertyIntegerTextField,
        defaultValue = { 25 },
    ),
    MarlothProperties.biome to PropertyDefinition(
        displayName = "Biome",
        widget = dropDownWidget { biomeIds },
        defaultValue = { Biomes.checkers },
    ),
    MarlothProperties.blockRotations to PropertyDefinition(
        displayName = "Block Rotations",
        widget = blockRotationsWidget,
        serialization = enumSerialization { BlockRotations.valueOf(it) },
        defaultValue = { BlockRotations.all },
    ),
) + commonPropertyDefinitions()
