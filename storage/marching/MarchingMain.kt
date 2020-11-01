package marloth.clienting.rendering.marching

import marloth.clienting.rendering.marching.services.cellRemovalService
import marloth.clienting.rendering.marching.services.gatherNeededCells
import marloth.clienting.rendering.marching.services.renderNewCells
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.glowing.*
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.SceneLayers
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.shading.ObjectShaderConfig
import silentorb.mythic.lookinglass.shading.ShaderFeatureConfig

//fun updateMarchingGpu(vertexSchema: VertexSchema, sources: CellSourceMeshes, state: MarchingGpuState): MarchingGpuState {
//  val newGpuMeshes = cellMeshesToGpuMeshes(vertexSchema, sources)
//  return state.copy(
//      meshes = state.meshes + newGpuMeshes
//  )
//}
//
//fun updateMarchingMain(
//    sceneRenderer: SceneRenderer,
//    models: Map<String, ModelFunction>,
//    idleTime: Long,
//    layers: SceneLayers,
//    marching: MarchingState
//): MarchingState {
//  val allElements = layers.flatMap { it.elements }
//  val vertexSchema = sceneRenderer.renderer.vertexSchemas.shadedColor
//  val timeLimits = allocateTimeLimits(idleTime, 1_500_000, marching.timeMeasurements)
//  val transformedModels = mapElementTransforms(models, filterModels(models, allElements))
//  val hitCells = gatherNeededCells(sceneRenderer.camera, transformedModels)
//  val cellsGreedy = marching.pendingCells + hitCells
//  val newPending = cellsGreedy - marching.gpu.meshes.keys
//  val (newSources, sourcesTime) = measureTime {
//    val gate = newTimeGate(timeLimits[renderCellsService] ?: 2_000_000)
//    renderNewCells(gate, transformedModels, newPending)
//  }
//  if (getDebugBoolean("DRAW_MARCHING_CENTER_HIT")) {
//    renderMarchingLab(sceneRenderer, models, sceneRenderer.camera, allElements)
//  }
//  val nextMarchingGpu = updateMarchingGpu(vertexSchema, newSources, marching.gpu)
//
//  val now = getNanoTime()
//  val nextLastUsed = marching.lastUsed + cellsGreedy.associateWith { now }
//  val nextMarching = marching.copy(
//      pendingCells = newPending - newSources.keys,
//      gpu = nextMarchingGpu,
//      timeMeasurements = mapOf(renderCellsService to sourcesTime),
//      lastUsed = nextLastUsed
//  )
//
//  return cellRemovalService(now, nextMarching)
//}
//
//fun drawMarchingMeshes(renderer: Renderer, meshes: Collection<GeneralMesh>) {
//  if (meshes.none())
//    return
//
//  val vertexSchema = renderer.vertexSchemas.shadedColor
//
//  val effect = renderer.getShader(vertexSchema, ShaderFeatureConfig(
//      colored = true,
//      shading = true
//  ))
//  effect.activate(ObjectShaderConfig())
//  globalState.depthEnabled = true
//  for (mesh in meshes) {
//    drawMesh(mesh, DrawMethod.triangleFan)
//  }
//}
//
//fun drawMarching(renderer: Renderer, state: MarchingGpuState) {
//  drawMarchingMeshes(renderer, state.meshes.values)
//}
