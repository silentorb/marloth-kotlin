package marloth.clienting.rendering

import marloth.scenery.enums.MeshId
import silentorb.imp.campaign.getModulesExecutionArtifacts
import silentorb.imp.campaign.loadWorkspace
import silentorb.imp.core.Context
import silentorb.imp.core.Dungeon
import silentorb.imp.core.FunctionImplementationMap
import silentorb.imp.execution.Library
import silentorb.imp.execution.combineLibraries
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.library.standard.standardLibrary
import silentorb.imp.parsing.parser.parseToDungeon
import silentorb.mythic.drawing.createCircleList
import silentorb.mythic.ent.mapEntry
import silentorb.mythic.fathom.fathomLibrary
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.fathom.sampling.SamplingConfig
import silentorb.mythic.fathom.sampling.sampleForm
import silentorb.mythic.fathom.surfacing.GridBounds
import silentorb.mythic.fathom.surfacing.getSceneDecimalBounds
import silentorb.mythic.fathom.surfacing.getSceneGridBounds
import silentorb.mythic.fathom.surfacing.old.marching.marchingMesh
import silentorb.mythic.glowing.*
import silentorb.mythic.imaging.texturing.texturingLibrary
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.meshes.*
import silentorb.mythic.lookinglass.meshes.loading.loadGltf
import silentorb.mythic.resource_loading.getUrlPath
import silentorb.mythic.scenery.Box
import silentorb.mythic.scenery.MeshName
import java.io.File
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

fun newImpLibrary(): Library =
    combineLibraries(
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

fun sampleModelOld(library: Library, vertexSchema: VertexSchema): (String, String) -> ModelMesh {
  val context = listOf(library.namespace)
  val functions = library.implementation

  return { name, code ->
    val (dungeon, errors) = parseToDungeon("", context)(code)
    if (errors.any())
      throw Error(errors.first().message.toString())

    val graph = dungeon.graph
    val model = executeToSingleValue(context, functions, graph)!! as ModelFunction

    val bounds = getSceneGridBounds(model.form, 1f)
        .pad(1)

    val decimalBounds = getSceneDecimalBounds(model.form)
    val dimensions = decimalBounds.end - decimalBounds.start

    val config = SamplingConfig(
        getDistance = model.form,
        getShading = model.shading,
        pointSizeScale = 5f,
//        pointSize = 8f,
        resolution = 4,
        levels = 3
    )

    val sampledModel = sampleGeneralMesh(vertexSchema, config, bounds, defaultLodRanges.takeLast(config.levels))

    ModelMesh(
        id = name,
        sampledModel = sampledModel,
        bounds = Box(dimensions / 2f)
    )
  }
}

fun sampleModel(context: Context, functions: FunctionImplementationMap, vertexSchema: VertexSchema): (String, Dungeon) -> ModelMesh =
    { name, dungeon ->
      val graph = dungeon.graph
      val model = executeToSingleValue(context, functions, graph)!! as ModelFunction
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

      val collision = model.collision

      ModelMesh(
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
          bounds = if (collision != null) collision(getSceneDecimalBounds(model.form)) else null
      )
    }

fun createMeshes(vertexSchemas: VertexSchemas): Pair<Map<MeshName, ModelMesh>, List<Armature>> {
  val library = newImpLibrary()
  val imports = importedMeshes(vertexSchemas)
  val workspaceUrl = Thread.currentThread().contextClassLoader.getResource("models/workspace.yaml")!!
  val (workspace, campaignErrors, parsingErrors) = loadWorkspace(library, Paths.get(workspaceUrl.toURI()).parent)
  val modules = workspace.modules
  val (context, functions) = getModulesExecutionArtifacts(library.implementation, listOf(library.namespace), modules)
  val assets = modules["models"]!!.dungeons
  val models = assets
      .mapValues(mapEntry(sampleModel(context, functions, vertexSchemas.shadedColor)))

  val importedMeshes = imports.flatMap { it.meshes }.associateBy { it.id }

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
