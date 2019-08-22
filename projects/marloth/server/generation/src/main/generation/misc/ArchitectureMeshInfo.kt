package generation.misc

import scenery.MeshName
import scenery.Shape

enum class MeshAttribute {
  canHaveAttachment,
  canFlipHorizontally,
  canFlipUpsideDown,
  canRotateOnSide
}

typealias MeshAttributes = Set<MeshAttribute>

data class ArchitectureMeshInfo(
    val shape: Shape,
    val attributes: MeshAttributes
)

typealias MeshInfoMap = Map<MeshName, ArchitectureMeshInfo>

typealias MeshShapeMap = Map<MeshName, Shape>

typealias MeshAttributeMap = Map<MeshName, MeshAttributes>

fun compileArchitectureMeshInfo(shapes: MeshShapeMap, attributes: MeshAttributeMap): MeshInfoMap =
    shapes.mapValues { (key, shape) ->
      val info = ArchitectureMeshInfo(
          shape = shape,
          attributes = attributes[key] ?: setOf()
      )
      info
    }
