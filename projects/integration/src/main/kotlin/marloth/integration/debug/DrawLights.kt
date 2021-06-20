package marloth.integration.debug

import marloth.scenery.enums.MeshId
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.glowing.DrawMethod
import silentorb.mythic.glowing.drawMesh
import silentorb.mythic.glowing.globalState
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.shading.ObjectShaderConfig
import silentorb.mythic.lookinglass.shading.ShaderFeatureConfig
import silentorb.mythic.scenery.Light
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector4

fun drawLights(lights: List<Light>, renderer: Renderer) {
  val effect = renderer.getShader(renderer.vertexSchemas.flat, ShaderFeatureConfig())
  val sphere = renderer.meshes[MeshId.sphere]!!.primitives.first()
  for (light in lights) {
    effect.activate(ObjectShaderConfig(
        color = Vector4(1f, 1f, 0f, 0.3f),
        transform = Matrix.identity
            .translate(light.offset)
            .scale(0.6f)

    ))
    globalState.depthEnabled = false
    drawMesh(sphere.mesh, DrawMethod.triangleFan)
    globalState.depthEnabled = true
  }
}

fun conditionalDrawLights(lights: List<Light>, renderer: Renderer) {
  if (getDebugBoolean("DRAW_LIGHTS")) {
    drawLights(lights, renderer)
  }
}
