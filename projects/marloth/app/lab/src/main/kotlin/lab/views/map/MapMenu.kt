package lab.views.map

import lab.LabCommandType
import mythic.bloom.Menu
import mythic.bloom.MenuItem
import mythic.bloom.menuBar
import mythic.bloom.next.Flower

val mapMenu: Flower = menuBar(mapTextStyle, listOf(
    Menu(
        name = "View",
        character = "v",
        items = listOf(
            MenuItem(
                name = "Normals",
                value = LabCommandType.toggleNormals
            ),
            MenuItem(
                name = "Node ids",
                value = LabCommandType.toggleNodeIds
            ),
            MenuItem(
                name = "Solid",
                value = LabCommandType.toggleMeshDisplay
            ),
            MenuItem(
                name = "Wireframe",
                value = LabCommandType.toggleWireframe
            ),
            MenuItem(
                name = "Face ids",
                value = LabCommandType.toggleFaceIds
            ),
            MenuItem(
                name = "Isolate selection",
                value = LabCommandType.toggleIsolateSelection
            ),
            MenuItem(
                name = "Abstract",
                value = LabCommandType.toggleAbstract
            ),
            MenuItem(
                name = "Camera mode",
                value = LabCommandType.switchCamera
            )
        )
    ),
    Menu(
        name = "World",
        character = "w",
        items = listOf(
            MenuItem(
                name = "Rebuild",
                value = LabCommandType.rebuildWorld
            )
        )
    )
))
