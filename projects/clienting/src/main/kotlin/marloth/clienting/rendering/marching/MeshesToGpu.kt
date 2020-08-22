package marloth.clienting.rendering.marching

import silentorb.mythic.glowing.*
import silentorb.mythic.lookinglass.serializeVertex

fun meshToGpu(vertexSchema: VertexSchema, data: MarchingModelMesh): GeneralMesh {
  val vertices = data.vertices
  val triangles = data.triangles
  val vertexFloats = vertices
      .flatMap(::serializeVertex)
      .toFloatArray()

  val indices = createIntBuffer(triangles.flatten())
  return GeneralMesh(
      vertexSchema = vertexSchema,
      vertexBuffer = newVertexBuffer(vertexSchema).load(createFloatBuffer(vertexFloats)),
      count = vertices.size / vertexSchema.floatSize,
      indices = indices,
      primitiveType = PrimitiveType.triangles
  )
}

fun cellMeshesToGpuMeshes(vertexSchema: VertexSchema, sources: CellSourceMeshes): CellGpuMeshes = sources
    .mapValues { (_, mesh) ->
      meshToGpu(vertexSchema, mesh)
    }

fun synchronizeCellGpuMeshes(vertexSchema: VertexSchema, sources: CellSourceMeshes, gpuMeshes: CellGpuMeshes): CellGpuMeshes {
  val missing = sources - gpuMeshes.keys
  val newMeshes = missing
      .mapValues { (_, mesh) ->
        meshToGpu(vertexSchema, mesh)
      }

  return gpuMeshes + newMeshes
}
