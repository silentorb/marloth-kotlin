package generation.abstract

import simulation.*

fun faceNodes(info: ConnectionFace) =
    listOf(info.firstNode, info.secondNode)
