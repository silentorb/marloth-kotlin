package rendering.drawing

import mythic.glowing.DrawMethod
import mythic.glowing.drawMesh
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import rendering.Renderer
import rendering.meshes.Primitive
import rendering.shading.ObjectShaderConfig
import rendering.shading.ShaderFeatureConfig

fun drawPrimitive(renderer: Renderer, primitive: Primitive, transform: Matrix, color: Vector4? = null){
  val material = primitive.material
  val texture = renderer.textures[material.texture]
  val effect = renderer.getShader(primitive.mesh.vertexSchema, ShaderFeatureConfig(
      texture = texture != null
  ))
  effect.activate(ObjectShaderConfig(
      transform,
      color = color ?: material.color,
      glow = material.glow,
      normalTransform = Matrix(),
      texture = texture
  ))
  drawMesh(primitive.mesh, DrawMethod.triangleFan)
}
