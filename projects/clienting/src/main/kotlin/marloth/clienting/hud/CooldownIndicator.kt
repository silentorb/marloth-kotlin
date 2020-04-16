package marloth.clienting.hud

import org.lwjgl.opengl.GL11
import silentorb.mythic.bloom.Depiction
import silentorb.mythic.bloom.centered
import silentorb.mythic.bloom.depict
import silentorb.mythic.bloom.next.*
import silentorb.mythic.drawing.SingleColorShader
import silentorb.mythic.drawing.createCircleList
import silentorb.mythic.drawing.getStaticCanvasDependencies
import silentorb.mythic.drawing.transformScalar
import silentorb.mythic.glowing.GeneralMesh
import silentorb.mythic.glowing.PrimitiveType
import silentorb.mythic.glowing.createFloatBuffer
import silentorb.mythic.glowing.newVertexBuffer
import silentorb.mythic.spatial.*

const val cooldownCircleResolution = 64
const val cooldownMeshKey = "cooldown"

fun createCooldownCircleMesh(): GeneralMesh {
  val dependencies = getStaticCanvasDependencies()
  val vertexSchema = dependencies.vertexSchemas.simple
  val values = listOf(0f, 0f).plus(createCircleList(1f, cooldownCircleResolution, Pi, -1f))
  return GeneralMesh(
      vertexSchema = vertexSchema,
      primitiveType = PrimitiveType.triangles,
      vertexBuffer = newVertexBuffer(vertexSchema).load(createFloatBuffer(values)),
      count = values.size / 2
  )
}

fun drawCooldown(pixelsToScalar: Matrix, mesh: GeneralMesh, shader: SingleColorShader,
                 position: Vector2, radius: Float, color: Vector4, completion: Float) {
  val pointCount = mesh.count!! - 1
  val range = (pointCount.toFloat() * completion).toInt()
  val finalCount = range + 1 // Add one for the center vertex
  if (finalCount != 0) {
    val transform = transformScalar(pixelsToScalar, position, Vector2(radius, radius))
    shader.activate(transform, color)
    mesh.vertexBuffer.activate()
    GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, finalCount)
  }
}

fun cooldownIndicator(radius: Float, color: Vector4, completion: Float): Depiction = { bounds, canvas ->
  val mesh = canvas.custom[cooldownMeshKey] as GeneralMesh
  val position = bounds.position.toVector2()
  drawCooldown(canvas.pixelsToScalar, mesh, canvas.effects.singleColorShader, position, radius, color, completion)
}

fun cooldownIndicatorPlacement(completion: Float): Flower {
  return div("b",
      reverse = reverseOffset(left = centered, top = centered))(
      div(forward = forwardDimensions(fixed(30), fixed(30)))(
          depict(cooldownIndicator(50f, Vector4(1f, 1f, 1f, 0.3f), completion))
      )
  )
}
