package marloth.clienting.rendering

import marloth.scenery.enums.MeshId
import silentorb.imp.campaign.codeFromFile
import silentorb.imp.campaign.getModulesExecutionArtifacts
import silentorb.imp.campaign.loadModules
import silentorb.imp.campaign.loadWorkspace
import silentorb.imp.core.*
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.library.standard.standardLibrary
import silentorb.mythic.drawing.createCircleList
import silentorb.mythic.fathom.fathomLibrary
import silentorb.mythic.fathom.marching.marchingMesh
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.glowing.*
import silentorb.mythic.imaging.texturing.texturingLibrary
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.meshes.*
import silentorb.mythic.lookinglass.meshes.loading.loadGltf
import silentorb.mythic.resource_loading.getUrlPath
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.scenery.SamplePoint
import silentorb.mythic.scenery.Shape
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

fun listFiles(path: Path): List<Path> =
    Files.list(path)
        .use { paths ->
          paths
              .toList()
              .filterIsInstance<Path>()
        }

fun getModelFilenames(): List<Path> {
  val modelRoot = getUrlPath("models")
  val files = listFiles(modelRoot)
  return files
}

fun getMeshFilenames(): List<Path> {
  val modelRoot = getUrlPath("gltf")
  val files = listFiles(modelRoot)
  return files
}

fun importedMeshes(vertexSchemas: VertexSchemas) =
    getMeshFilenames()
        .map { it.fileName.toString() }
        .map { loadGltf(vertexSchemas, it, "gltf/" + it + "/" + it) }

fun createHollowCircleMesh(vertexSchema: VertexSchema, resolution: Int): GeneralMesh {
  val values2d = createCircleList(1f, resolution, 0f, -1f)
  val values3d = (0 until values2d.size step 2).flatMap { listOf(values2d[it], values2d[it + 1], 0f) }

  return GeneralMesh(
      vertexSchema = vertexSchema,
      primitiveType = PrimitiveType.loops,
      vertexBuffer = newVertexBuffer(vertexSchema).load(createFloatBuffer(values3d)),
      count = resolution
  )
}

fun newImpLibrary() =
    listOf(
        defaultImpNamespace(),
        standardLibrary(),
//      auraLibrary(),
        texturingLibrary(),
        fathomLibrary()
    )

val defaultLodRanges: LodRanges = listOf(
    60f,
    25f,
    10f
)

data class DeferredImpMesh(
    val name: String,
    val model: ModelFunction
)

data class LoadedMeshData(
    val name: String,
    val vertices: List<SamplePoint>,
    val triangles: List<List<Int>>,
    val collision: Shape?
)

fun compileModel(context: Context, key: PathKey): DeferredImpMesh {
  val value = executeToSingleValue(context, key)!!
  val model = value as ModelFunction
  return DeferredImpMesh(
      name = key.name,
      model = model
  )
}

fun sampleModel(deferred: DeferredImpMesh): LoadedMeshData {
  val model = deferred.model
  val name = deferred.name
  val voxelsPerUnit = 10
  val (vertices, triangles) = marchingMesh(voxelsPerUnit, model.form, model.shading)
  return LoadedMeshData(
      name = name,
      vertices = vertices,
      triangles = triangles,
      collision = model.collision
  )
}

fun sampleModels(deferred: List<DeferredImpMesh>): List<LoadedMeshData> =
    deferred.map(::sampleModel)

fun meshToGpu(vertexSchema: VertexSchema, data: LoadedMeshData): ModelMesh {
  val vertices = data.vertices
  val triangles = data.triangles
  val name = data.name
  val collision = data.collision
  val vertexFloats = vertices
      .flatMap(::serializeVertex)
      .toFloatArray()

  val indices = createIntBuffer(triangles.flatten())
  val mesh = GeneralMesh(
      vertexSchema = vertexSchema,
      vertexBuffer = newVertexBuffer(vertexSchema).load(createFloatBuffer(vertexFloats)),
      count = vertices.size / vertexSchema.floatSize,
      indices = indices,
      primitiveType = PrimitiveType.triangles
  )

  return ModelMesh(
      id = name,
      primitives = listOf(
          Primitive(
              mesh = mesh,
              material = Material(
                  drawMethod = DrawMethod.triangles,
                  shading = true,
                  coloredVertices = true
              )
          )
      ),
      bounds = collision
  )
}

fun meshesToGpu(vertexSchema: VertexSchema): (List<LoadedMeshData>) -> Map<String, ModelMesh> = { meshes ->
  meshes.associate { mesh ->
    mesh.name to meshToGpu(vertexSchema, mesh)
  }
}

fun gatherImpMeshes(): List<DeferredImpMesh> {
  val initialContext = newImpLibrary()
  val workspaceUrl = Thread.currentThread().contextClassLoader.getResource("models/workspace.yaml")!!
  val (workspace, errors) = loadWorkspace(Paths.get(workspaceUrl.toURI()).parent)
  val (modules) = loadModules(workspace, initialContext, codeFromFile)
  val context = getModulesExecutionArtifacts(initialContext, modules)
  val outputs = getGraphOutputNodes(mergeNamespaces(context))
      .filter { it.path == "models" }
  return outputs
      .map { compileModel(context, it) }
}

typealias MeshLoadingState = LoadingState<DeferredImpMesh, LoadedMeshData>

fun updateAsyncMeshLoading(vertexSchema: VertexSchema) = updateAsyncLoading(::sampleModels, meshesToGpu(vertexSchema))

fun createMeshes(vertexSchemas: VertexSchemas): Pair<Map<MeshName, ModelMesh>, List<Armature>> {
  val imports = importedMeshes(vertexSchemas)
  val importedMeshes = imports.flatMap { it.meshes }.associateBy { it.id }
  val customMeshes = mapOf(
      MeshId.hollowCircle to createHollowCircleMesh(vertexSchemas.flat, 64),
      "line" to createLineMesh(vertexSchemas.flat),
      "billboard" to createBillboardMesh(vertexSchemas.billboard)
  )
      .mapValues { createModelElements(it.value) }
      .mapValues { ModelMesh(it.key, it.value) }
  val meshes = importedMeshes + customMeshes

  val armatures = imports.flatMap { it.armatures }
  return Pair(meshes, armatures)
}
