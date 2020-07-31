package generation.architecture.engine

import marloth.scenery.enums.ArchitectureMeshInfo
import marloth.scenery.enums.MeshInfoMap
import marloth.scenery.enums.MeshShapeMap

fun compileArchitectureMeshInfo(shapes: MeshShapeMap): MeshInfoMap {
  return shapes.mapValues { (_, shape) ->
    val info = ArchitectureMeshInfo(
        shape = shape
    )
    info
  }
}
