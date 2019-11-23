package generation.architecture.misc

import generation.architecture.definition.MeshAttribute
import generation.architecture.definition.meshAttributesThatRequireAShape
import scenery.MeshName
import scenery.Shape

typealias MeshAttributes = Set<MeshAttribute>

data class ArchitectureMeshInfo(
    val shape: Shape?,
    val attributes: MeshAttributes
)

typealias MeshInfoMap = Map<MeshName, ArchitectureMeshInfo>

typealias MeshShapeMap = Map<MeshName, Shape>

typealias MeshAttributeMap = Map<MeshName, MeshAttributes>

fun compileArchitectureMeshInfo(shapes: MeshShapeMap, attributes: MeshAttributeMap): MeshInfoMap {
  val result = attributes.mapValues { (key, attributes) ->
    val shape = shapes[key]
    val info = ArchitectureMeshInfo(
        shape = shape,
        attributes = attributes
    )
    info
  }
  val missingShapes = result.filterValues { info ->
    info.shape == null && info.attributes.intersect(meshAttributesThatRequireAShape).any()
  }
  assert(missingShapes.none())
  return result
}
