package generation.abstracted

import simulation.*

fun faceNodes(info: ConnectionFace) =
    listOf(info.firstNode, info.secondNode)
