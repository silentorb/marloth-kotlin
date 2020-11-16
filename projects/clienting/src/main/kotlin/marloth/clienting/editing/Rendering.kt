package marloth.clienting.editing

import marloth.clienting.Client
import marloth.clienting.rendering.defaultLightingConfig
import silentorb.mythic.editing.Editor
import silentorb.mythic.editing.lookinglass.renderEditor
import silentorb.mythic.lookinglass.texturing.updateAsyncTextureLoading
import silentorb.mythic.platforming.WindowInfo

fun renderEditorViewport(client: Client, windowInfo: WindowInfo, editor: Editor) {
  val renderer = client.renderer
  updateAsyncTextureLoading(client.textureLoadingState, renderer.textures)
  renderEditor(client.renderer, windowInfo, editor, defaultLightingConfig())
  client.platform.display.swapBuffers()
}
