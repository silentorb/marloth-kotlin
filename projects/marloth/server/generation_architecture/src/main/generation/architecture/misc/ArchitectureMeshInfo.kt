package generation.architecture.misc

import generation.architecture.definition.MeshAttribute
import scenery.MeshName
import scenery.Shape

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
