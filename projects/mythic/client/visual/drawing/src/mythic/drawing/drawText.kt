package mythic.drawing

import mythic.glowing.DrawMethod
import mythic.glowing.VertexSchema
import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import mythic.typography.TextPackage
import mythic.typography.prepareText
import mythic.typography.unitConversion
import org.joml.Vector2i
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL14.GL_BLEND_SRC_ALPHA

fun activateTexture(texture: Int) {
  glActiveTexture(GL_TEXTURE0)
  glBindTexture(GL_TEXTURE_2D, texture)
}

fun getUnitScaling(dimensions: Vector2i) =
    if (dimensions.x < dimensions.y)
      Vector2(1f, dimensions.x.toFloat() / dimensions.y)
    else
      Vector2(dimensions.y.toFloat() / dimensions.x, 1f)

fun renderText(config: TextConfiguration, effect: ColoredImageShader, textPackage: TextPackage, pixelsToScalar: Matrix) {
  val position = config.position
  val scale = config.size * 0.05f

  val transform = Matrix()
      .mul(pixelsToScalar)
//      .translate(position.x, position.y, 0f)
      .scale(scale, scale, 1f)

  effect.activate(transform, config.color)

//  glow.check_error("setting text values")

  activateTexture(config.font.texture)

  glEnable(GL_BLEND)
  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
  textPackage.mesh.draw(DrawMethod.triangleFan)
}

fun drawTextRaw(config: TextConfiguration, effect: ColoredImageShader, vertexSchema: VertexSchema, pixelsToScalar: Matrix) {
  val textPackage = prepareText(config, vertexSchema)
  if (textPackage != null) {
    renderText(config, effect, textPackage, pixelsToScalar)
    textPackage.mesh.dispose()
  }
}