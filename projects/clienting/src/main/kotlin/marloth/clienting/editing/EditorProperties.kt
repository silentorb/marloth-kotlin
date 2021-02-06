package marloth.clienting.editing

import generation.architecture.biomes.BiomeId
import generation.general.CellDirection
import generation.general.Direction
import generation.general.StandardHeights
import generation.general.directionNames
import imgui.ImGui
import marloth.definition.misc.blockSides
import silentorb.mythic.editing.*
import silentorb.mythic.editing.components.dropDownWidget
import silentorb.mythic.editing.components.integerTextField
import silentorb.mythic.editing.components.propertyIntegerTextField
import silentorb.mythic.editing.components.wrapSimpleWidget
import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Serialization
import silentorb.mythic.ent.reflectProperties
import silentorb.mythic.spatial.Vector3i
import simulation.misc.MarlothProperties

val cellDirectionWidget: PropertyWidget = { _, entry, owner ->
  val value = entry.target as CellDirection
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

val biomeIds = reflectProperties<String>(BiomeId)

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
        defaultValue = { Direction.east.name },
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
    MarlothProperties.myBiome to PropertyDefinition(
        displayName = "My Biome",
        widget = dropDownWidget { biomeIds },
        defaultValue = { BiomeId.checkers },
    ),
    MarlothProperties.otherBiome to PropertyDefinition(
        displayName = "Other Biome",
        widget = dropDownWidget { biomeIds },
        defaultValue = { BiomeId.checkers },
    ),
) + commonPropertyDefinitions()
