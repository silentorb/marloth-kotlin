package generation.architecture.engine

import marloth.scenery.enums.ArchitectureMeshInfo
import marloth.scenery.enums.MeshAttributeMap
import marloth.scenery.enums.MeshInfoMap
import marloth.scenery.enums.MeshShapeMap

fun compileArchitectureMeshInfo(shapes: MeshShapeMap, attributes: MeshAttributeMap): MeshInfoMap {
  return shapes.mapValues { (key, shape) ->
    val info = ArchitectureMeshInfo(
        shape = shape,
        attributes = attributes[key] ?: setOf()
    )
    info
  }
}
