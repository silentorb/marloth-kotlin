package silentorb.raymarching

import mythic.imaging.*
import mythic.spatial.Vector2
import mythic.spatial.toVector2
import org.joml.Vector2i
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer
import java.nio.FloatBuffer

fun newMarchBuffers(area: Int) =
    MarchedBuffers(
        color = BufferUtils.createFloatBuffer(area * 3),
        depth = BufferUtils.createFloatBuffer(area),
        position = BufferUtils.createFloatBuffer(area * 3),
        normal = BufferUtils.createFloatBuffer(area * 3)
    )

fun rewindMarchBuffers(buffers: MarchedBuffers) {
  buffers.color.rewind()
  buffers.depth.rewind()
  buffers.position.rewind()
  buffers.normal.rewind()
}

fun renderToMarchBuffers(buffers: MarchedBuffers, marcher: Marcher, scene: Scene, dimensions: Vector2i) {
  val cameraTransform = newCameraTransform(scene.camera)
  val render = pixelRenderer(marcher, scene, cameraTransform)
  rewindMarchBuffers(buffers)

  val aspect = 1 / 1.333f
  val mod = Vector2(1f) * 2f / dimensions.toVector2()

  for (y in 0 until dimensions.y) {
    for (x in 0 until dimensions.x) {
      val input = Vector2(
          (x.toFloat() * mod.x - 1f) * aspect,
          1f - y.toFloat() * mod.y
      )
      val point = render(input)
      buffers.color.put(point.color)
      buffers.depth.put(point.depth)
      buffers.position.put(point.position)
      buffers.normal.put(point.normal)
    }
  }

  rewindMarchBuffers(buffers)
}

fun postPipeline(dimensions: Vector2i, buffers: MarchedBuffers, output: ByteBuffer) {
  val illumination = illuminationOperator(dimensions)(1, mapOf(
      "depth" to buffers.depth,
      "position" to buffers.position,
      "normal" to buffers.normal
  )) as FloatBuffer

  val toned = toneMapping(dimensions)(2, mapOf(
      "input" to illumination
  )) as FloatBuffer

  val mix = mixSceneOperator(dimensions)(3, mapOf(
      "color" to buffers.color,
      "illumination" to toned
  )) as FloatBuffer

  rgbFloatToBytes(mix, output)
}