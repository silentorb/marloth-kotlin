package lab.views.map

import silentorb.mythic.haft.DeviceIndex
import silentorb.mythic.haft.MOUSE_SCROLL_DOWN
import silentorb.mythic.haft.MOUSE_SCROLL_UP
import silentorb.mythic.haft.createBindings
import lab.LabCommandType
import org.lwjgl.glfw.GLFW

fun mapViewBindings() =
    createBindings(DeviceIndex.keyboard, mapOf(
        GLFW.GLFW_KEY_UP to LabCommandType.moveUp,
        GLFW.GLFW_KEY_DOWN to LabCommandType.moveDown,
        GLFW.GLFW_KEY_LEFT to LabCommandType.moveLeft,
        GLFW.GLFW_KEY_RIGHT to LabCommandType.moveRight,
        GLFW.GLFW_KEY_W to LabCommandType.moveUp,
        GLFW.GLFW_KEY_S to LabCommandType.moveDown,
        GLFW.GLFW_KEY_A to LabCommandType.moveLeft,
        GLFW.GLFW_KEY_D to LabCommandType.moveRight,
        GLFW.GLFW_KEY_Q to LabCommandType.rotateLeft,
        GLFW.GLFW_KEY_E to LabCommandType.rotateRight,
        GLFW.GLFW_KEY_R to LabCommandType.rotateUp,
        GLFW.GLFW_KEY_F to LabCommandType.rotateDown,
        GLFW.GLFW_KEY_EQUAL to LabCommandType.zoomIn,
        GLFW.GLFW_KEY_MINUS to LabCommandType.zoomOut,
        GLFW.GLFW_KEY_PAGE_UP to LabCommandType.zoomIn,
        GLFW.GLFW_KEY_PAGE_DOWN to LabCommandType.zoomOut
    ))
        .plus(createBindings(DeviceIndex.mouse, mapOf(
            GLFW.GLFW_MOUSE_BUTTON_1 to LabCommandType.select,
            MOUSE_SCROLL_UP to LabCommandType.zoomIn,
            MOUSE_SCROLL_DOWN to LabCommandType.zoomOut
        )))
