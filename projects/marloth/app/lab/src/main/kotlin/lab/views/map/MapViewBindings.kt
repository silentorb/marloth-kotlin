package lab.views.map

import haft.MOUSE_SCROLL_DOWN
import haft.MOUSE_SCROLL_UP
import haft.createBindings
import haft.createStrokeBindings
import lab.LabCommandType
import org.lwjgl.glfw.GLFW

fun mapViewBindings() =
    createStrokeBindings<LabCommandType>(0, mapOf(
        GLFW.GLFW_KEY_M to LabCommandType.toggleMeshDisplay,
        GLFW.GLFW_KEY_N to LabCommandType.toggleNormals,
        GLFW.GLFW_KEY_I to LabCommandType.toggleFaceIds,
        GLFW.GLFW_KEY_O to LabCommandType.toggleNodeIds,
        GLFW.GLFW_KEY_LEFT_BRACKET to LabCommandType.decrementRaySkip,
        GLFW.GLFW_KEY_RIGHT_BRACKET to LabCommandType.incrementRaySkip
    ))
        .plus(createBindings<LabCommandType>(0, mapOf(
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
        )))
        .plus(createBindings(1, mapOf(
            GLFW.GLFW_MOUSE_BUTTON_1 to LabCommandType.select,
            MOUSE_SCROLL_UP to LabCommandType.zoomIn,
            MOUSE_SCROLL_DOWN to LabCommandType.zoomOut
        )))