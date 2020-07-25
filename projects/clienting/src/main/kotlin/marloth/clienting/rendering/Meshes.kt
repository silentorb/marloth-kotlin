package marloth.clienting.rendering

import marloth.scenery.enums.MeshId
import silentorb.imp.campaign.codeFromFile
import silentorb.imp.campaign.getModulesExecutionArtifacts
import silentorb.imp.campaign.loadWorkspace
import silentorb.imp.core.*
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.library.standard.standardLibrary
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.drawing.createCircleList
import silentorb.mythic.fathom.fathomLibrary
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.fathom.sampling.SamplingConfig
import silentorb.mythic.fathom.sampling.sampleForm
import silentorb.mythic.fathom.surfacing.GridBounds
import silentorb.mythic.fathom.marching.marchingMesh
import silentorb.mythic.glowing.*
import silentorb.mythic.imaging.texturing.texturingLibrary
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.meshes.*
import silentorb.mythic.lookinglass.meshes.loading.loadGltf
import silentorb.mythic.resource_loading.getUrlPath
import silentorb.mythic.scenery.MeshName
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

fun sampleGeneralMesh(vertexSchema: VertexSchema, config: SamplingConfig, bounds: GridBounds, lodRanges: LodRanges): SampledModel {
  val initialPoints = sampleForm(config, bounds)
  return newSampledModel(vertexSchema, lodRanges, config.levels, initialPoints)
}

fun sampleModel(context: Context, vertexSchema: VertexSchema): (PathKey) -> ModelMesh =
    { key ->
      val value = executeToSingleValue(context, key)!!
      val model = value as ModelFunction
      val voxelsPerUnit = 10
      val (vertices, triangles) = marchingMesh(voxelsPerUnit, model.form, model.shading)
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

      ModelMesh(
          id = key.name,
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
          bounds = model.collision
      )
    }

fun impMeshes(vertexSchemas: VertexSchemas): Map<MeshName, ModelMesh> {
  val initialContext = newImpLibrary()
  val workspaceUrl = Thread.currentThread().contextClassLoader.getResource("models/workspace.yaml")!!
  val (workspace, errors) = loadWorkspace(codeFromFile, initialContext, Paths.get(workspaceUrl.toURI()).parent)
  val modules = workspace.modules
  val context = getModulesExecutionArtifacts(initialContext, modules)
  val outputs = getGraphOutputNodes(mergeNamespaces(context))
      .filter { it.path == "models" }
  return outputs
      .associateWith(sampleModel(context, vertexSchemas.shadedColor))
      .mapKeys { it.key.name }
}

fun createMeshes(vertexSchemas: VertexSchemas): Pair<Map<MeshName, ModelMesh>, List<Armature>> {
  val imports = importedMeshes(vertexSchemas)
  val importedMeshes = imports.flatMap { it.meshes }.associateBy { it.id }
  val models = if (getDebugBoolean("USE_IMP_MESHES"))
    impMeshes(vertexSchemas)
  else
    mapOf()

  val customMeshes = mapOf(
      MeshId.hollowCircle to createHollowCircleMesh(vertexSchemas.flat, 64),
      "line" to createLineMesh(vertexSchemas.flat),
      "billboard" to createBillboardMesh(vertexSchemas.billboard)
  )
      .mapValues { createModelElements(it.value) }
      .mapValues { ModelMesh(it.key, it.value) }
  val meshes = importedMeshes + customMeshes + models

  val armatures = imports.flatMap { it.armatures }
  return Pair(meshes, armatures)
}
