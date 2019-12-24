package lab.views.game

import marloth.scenery.enums.MeshId
import silentorb.mythic.debugging.getDebugSetting
import silentorb.mythic.glowing.DrawMethod
import silentorb.mythic.glowing.drawMesh
import silentorb.mythic.glowing.globalState
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.shading.ObjectShaderConfig
import silentorb.mythic.lookinglass.shading.ShaderFeatureConfig
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector4
import simulation.main.Deck

fun drawAiTargets(deck: Deck, renderer: Renderer) {
  val targets = deck.spirits.mapNotNull { it.value.pursuit.targetPosition }
  val effect = renderer.getShader(renderer.vertexSchemas.flat, ShaderFeatureConfig())
  val cube = renderer.meshes[MeshId.cube.name]!!.primitives.first()
  for (target in targets) {
    effect.activate(ObjectShaderConfig(
        color = Vector4(1f, 0f, 1f, 0.3f),
        transform = Matrix()
            .translate(target)
            .scale(0.6f)

    ))
    globalState.depthEnabled = false
    drawMesh(cube.mesh, DrawMethod.triangleFan)
    globalState.depthEnabled = true
  }
}

fun conditionalDrawAiTargets(deck: Deck, renderer: Renderer) {
  if (getDebugSetting("DRAW_AI_TARGETS") != "0") {
    drawAiTargets(deck, renderer)
  }
}
