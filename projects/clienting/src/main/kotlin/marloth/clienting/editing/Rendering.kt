package marloth.clienting.editing

import marloth.clienting.Client
import marloth.clienting.rendering.defaultLightingConfig
import silentorb.mythic.debugging.getDebugFloat
import silentorb.mythic.editing.Editor
import silentorb.mythic.editing.RenderingMode
import silentorb.mythic.editing.SelectionQuery
import silentorb.mythic.editing.getViewports
import silentorb.mythic.editing.lookinglass.renderEditor
import silentorb.mythic.lookinglass.texturing.updateAsyncTextureLoading
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.scenery.LightingConfig

fun renderEditorViewport(client: Client, windowInfo: WindowInfo, editor: Editor): SelectionQuery? {
  val renderer = client.renderer
  updateAsyncTextureLoading(client.textureLoadingState, renderer.textures)
  val renderingMode = getViewports(editor)?.values?.firstOrNull()?.renderingMode
  val lightingConfig = if (renderingMode == RenderingMode.flat)
    LightingConfig(
        ambient = 0.6f
    )
  else
    defaultLightingConfig()

  val selectionQuery = renderEditor(client.renderer, windowInfo, editor, lightingConfig)
  client.platform.display.swapBuffers()
  return selectionQuery
}
