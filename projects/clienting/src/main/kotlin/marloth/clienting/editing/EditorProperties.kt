package marloth.clienting.editing

import generation.general.CellDirection
import generation.general.Direction
import generation.general.directionNames
import imgui.ImGui
import marloth.definition.misc.blockSides
import silentorb.mythic.editing.*
import silentorb.mythic.editing.components.dropDownWidget
import silentorb.mythic.editing.components.integerTextField
import silentorb.mythic.editing.components.wrapSimpleWidget
import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Serialization
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Vector3i
import simulation.misc.GameAttributes
import simulation.misc.MarlothProperties

fun cellDirectionWidget(entry: Entry): CellDirection {
  val value = entry.target as CellDirection
  val cell = value.cell
  val owner = "${entry.source}.${entry.property}"
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
  return CellDirection(Vector3i(x, y, z), Direction.valueOf(direction))
}

val propertyCellDirectionWidget: PropertyWidget = wrapSimpleWidget(::cellDirectionWidget)

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

fun marlothEditorPropertyDefinitions(sides: List<String> = blockSides): PropertyDefinitions = mapOf(
    MarlothProperties.mine to PropertyDefinition(
        displayName = "Mine",
        widget = dropDownWidget { sides },
        defaultValue = { sides.firstOrNull() },
    ),
    MarlothProperties.other to PropertyDefinition(
        displayName = "Other",
        widget = dropDownWidget { sides },
        defaultValue = { sides.firstOrNull() },
    ),
    MarlothProperties.direction to PropertyDefinition(
        displayName = "Direction",
        serialization = cellDirectionSerialization,
        widget = propertyCellDirectionWidget,
        defaultValue = { Direction.east.name },
    ),
    MarlothProperties.showIfSideIsEmpty to PropertyDefinition(
        displayName = "Show if Side is Empty",
        serialization = cellDirectionSerialization,
        widget = propertyCellDirectionWidget,
        defaultValue = { null },
    ),
) + commonPropertyDefinitions()
