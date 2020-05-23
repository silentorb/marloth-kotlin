package marloth.clienting.rendering

import marloth.scenery.enums.MeshId
import silentorb.imp.execution.*
import silentorb.imp.library.implementation.standard.standardLibrary
import silentorb.imp.parsing.parser.parseText
import silentorb.mythic.drawing.createCircleList
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.mapEntry
import silentorb.mythic.glowing.GeneralMesh
import silentorb.mythic.glowing.PrimitiveType
import silentorb.mythic.glowing.createFloatBuffer
import silentorb.mythic.glowing.newVertexBuffer
import silentorb.mythic.imaging.fathoming.ModelFunction
import silentorb.mythic.imaging.fathoming.fathomLibrary
import silentorb.mythic.imaging.fathoming.sampling.SamplingConfig
import silentorb.mythic.imaging.fathoming.sampling.sampleFunction
import silentorb.mythic.imaging.fathoming.surfacing.GridBounds
import silentorb.mythic.imaging.fathoming.surfacing.getSceneDecimalBounds
import silentorb.mythic.imaging.fathoming.surfacing.getSceneGridBounds
import silentorb.mythic.imaging.texturing.texturingLibrary
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.meshes.*
import silentorb.mythic.lookinglass.meshes.loading.loadGltf
import silentorb.mythic.scenery.Box
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.scenery.Shape
import silentorb.mythic.spatial.toList
import java.io.File

fun getModelFilenames(): Array<File> {
  val modelRoot = getResourceUrl("models")
  val files = File(modelRoot!!.toURI()).listFiles()
  return files
}

fun getMeshFilenames(): Array<File> {
  val modelRoot = getResourceUrl("gltf")
  val files = File(modelRoot!!.toURI()).listFiles()
  return files
}

fun importedMeshes(vertexSchemas: VertexSchemas) =
    getMeshFilenames()
        .map { it.name }
        .map { loadGltf(vertexSchemas, it, "gltf/" + it + "/" + it) }
//    }

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

fun sampleGeneralMesh(vertexSchema: VertexSchema, config: SamplingConfig, bounds: GridBounds): GeneralMesh {
  val points = sampleFunction(config, bounds)
  val vertices = points
      .flatMap { toList(it.location) + toList(it.normal) + listOf(it.size) + toList(it.color) }
      .toFloatArray()
  return GeneralMesh(
      vertexSchema = vertexSchema,
      primitiveType = PrimitiveType.points,
      vertexBuffer = newVertexBuffer(vertexSchema).load(createFloatBuffer(vertices)),
      count = vertices.size / vertexSchema.floatSize
  )
}

fun sampleModel(library: Library, vertexSchema: VertexSchema): (String, String) -> ModelMesh {
  val context = listOf(library.namespace)
  val functions = library.implementation

  return { name, code ->
    val (dungeon, errors) = parseText(context)(code)
    if (errors.any())
      throw Error(errors.first().message.name)

    val graph = dungeon.graph
    val model = executeToSingleValue(functions, graph)!! as ModelFunction

    val config = SamplingConfig(
        getDistance = model.distance,
        getColor = model.color,
        resolution = 10,
        pointSize = 7f
    )

    val bounds = getSceneGridBounds(model.distance, 1f)
        .pad(1)

    val decimalBounds = getSceneDecimalBounds(model.distance)
    val dimensions = decimalBounds.end - decimalBounds.start

    val stages = mapOf(
        0f to 20,
        10f to 15,
        15f to 10,
        30f to 6,
        60f to 1
    )
    val lod = stages.mapValues { (_, resolution) ->
      sampleGeneralMesh(vertexSchema, config.copy(resolution = resolution), bounds)
    }
    ModelMesh(
        id = name,
        particleLod = lod,
        bounds = Box(dimensions / 2f)
    )
  }
}

fun createMeshes(vertexSchemas: VertexSchemas): Pair<Map<MeshName, ModelMesh>, List<Armature>> {
  val library = newImpLibrary()
  val imports = importedMeshes(vertexSchemas)
  val modelSources = getModelFilenames()
      .associate { Pair(toCamelCase(it.nameWithoutExtension), loadTextResource("models/${it.name}")) }
  val models = modelSources
      .mapValues(mapEntry(sampleModel(library, vertexSchemas.shadedPoint)))

  val importedMeshes = imports.flatMap { it.meshes }.associate { Pair(it.id, it) }

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
