
data class DeferredImpMesh(
    val name: String,
    val model: ModelFunction
)

data class LoadedMeshData(
    val name: String,
    val data: MarchingModelMesh,
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
  val voxelsPerUnit = getDebugInt("MESH_RESOLUTION") ?: 10
  val (vertices, triangles) = marchingMesh(voxelsPerUnit, model.form, model.shading)
  return LoadedMeshData(
      name = name,
      data = MarchingModelMesh(
          vertices = vertices,
          triangles = triangles
      ),
      collision = model.collision
  )
}

fun sampleModels(deferred: List<DeferredImpMesh>): List<LoadedMeshData> =
    deferred.map(::sampleModel)

//fun meshesToGpu(vertexSchema: VertexSchema): (List<LoadedMeshData>) -> Map<String, ModelMesh> = { meshes ->
//  meshes.associate { mesh ->
//    mesh.name to meshToGpu(vertexSchema, mesh)
//  }
//}

fun gatherImpModels(): Map<String, ModelFunction> {
  val initialContext = newImpLibrary()
  val workspaceUrl = Thread.currentThread().contextClassLoader.getResource("models/workspace.yaml")!!
  val (workspace, errors) = loadWorkspace(Paths.get(workspaceUrl.toURI()).parent)
  val (modules) = loadAllModules(workspace, initialContext, codeFromFile)
  val context = getModulesExecutionArtifacts(initialContext, modules)
  val outputs = getGraphOutputNodes(mergeNamespaces(context))
      .filter { it.path == "models" }

  return outputs
      .associate {
        val result = compileModel(context, it)
        result.name to result.model
      }
}

typealias MeshLoadingState = LoadingState<DeferredImpMesh, LoadedMeshData>
