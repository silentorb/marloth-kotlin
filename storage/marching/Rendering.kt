
fun renderLayersWithMarching(sceneRenderer: SceneRenderer, layers: SceneLayers, marchingGpu: MarchingGpuState) {
  renderSceneLayers(sceneRenderer, sceneRenderer.camera, layers) { _, _, layer ->
    if (layer.attributes.contains(marchingRenderLayer)) {
      drawMarching(sceneRenderer.renderer, marchingGpu)
    }
  }
}
