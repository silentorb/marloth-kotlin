package silentorb.raymarching

import kotlinx.coroutines.*
import silentorb.mythic.imaging.*
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.toVector2
import org.joml.Vector2i
import org.lwjgl.BufferUtils
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

fun sliceBuffers(buffers: MarchedBuffers): (Int, Int) -> MarchedBuffers = { offset, length ->
  fun slice(buffer: FloatBuffer, stride: Int): FloatBuffer =
      buffer.position(offset * stride).slice()//.limit(length * stride)

  MarchedBuffers(
      color = slice(buffers.color, 3),
      depth = slice(buffers.depth, 1),
      position = slice(buffers.position, 3),
      normal = slice(buffers.normal, 3)
  )
}

fun hookCounter(): () -> Int {
  var counter = 0
  return {
    counter++
  }
}

fun renderSection(horizontal: IntRange, vertical: IntRange, mod: Vector2, render: PixelRenderer, buffers: MarchedBuffers) {
  val aspect = 1 / 1.333f
  val marchedPoint = MarchedPoint()
  for (y in vertical) {
    for (x in horizontal) {
      val input = Vector2(
          (x.toFloat() * mod.x - 1f) * aspect,// * aspect
          1f - y.toFloat() * mod.y
      )
      render(input, marchedPoint)
      buffers.color.put(marchedPoint.color)
      buffers.depth.put(marchedPoint.depth)
      buffers.position.put(marchedPoint.position)
      buffers.normal.put(marchedPoint.normal)
    }
  }
}

private const val renderThreadCount: Int = 1

val renderThreadPool = newFixedThreadPoolContext(renderThreadCount, "RenderThread")

suspend fun renderSections(mod: Vector2, render: PixelRenderer, buffers: MarchedBuffers, dimensions: Vector2i) {
  val localSlice = sliceBuffers(buffers)
  val sliceCount = renderThreadCount
  val area = dimensions.x * dimensions.y
  val sliceSize = area / sliceCount
  val slices = (0 until sliceCount).map { localSlice(it * sliceSize, sliceSize) }
  val sliceHeight = sliceSize / dimensions.x
  val tasks = slices.mapIndexed { index, slice ->
    val startY = index * sliceHeight
    val endY = startY + sliceHeight
    GlobalScope.async(renderThreadPool) {
      //      println("Start $index")
      renderSection((0 until dimensions.x), (startY until endY), mod, render, slice)
//      println("End $index")
    }
  }
  tasks.map { it.await() }//.reduce { acc, job -> acc. + job }
}

val tj = FloatArray(1200 * 1200)
fun renderToMarchBuffers(buffers: MarchedBuffers, marcher: Marcher, scene: Scene, cast: RayCaster, dimensions: Vector2i) {
  val calls = mutableListOf(0, 0)
  val render = pixelRenderer(marcher, scene, cast, { calls[0] += 1 }) { calls[1] += 1 }

  rewindMarchBuffers(buffers)

  val aspect = 1 / 1.333f
  val mod = Vector2(1f) * 2f / dimensions.toVector2()
//  println("Starting")
  if (renderThreadCount > 1) {
    runBlocking {
      renderSections(mod, render, buffers, dimensions)
    }
  }
//  println("Ending")
  else {
    var i = 0
    val marchedPoint = MarchedPoint()
    for (y in 0 until dimensions.y) {
      for (x in 0 until dimensions.x) {
        val input = Vector2(
            (x.toFloat() * mod.x - 1f),// * aspect
            1f - y.toFloat() * mod.y
        )
        render(input, marchedPoint)
//        tj[i] = marchedPoint.color.x
//        ++i
//        buffers.color.put(marchedPoint.color)
//        buffers.depth.put(marchedPoint.depth)
//        buffers.position.put(marchedPoint.position)
//        buffers.normal.put(marchedPoint.normal)
      }
    }
//    for (y in 0 until dimensions.y) {
//      for (x in 0 until dimensions.x) {
//        tj[i] = 0f
//        ++i
//      }
////      i = 0
//    }

    for (i2 in 0 until 1200 * 1200) {
      tj[i2] = 0f
    }
  }

  rewindMarchBuffers(buffers)
}

fun postPipeline(dimensions: Vector2i, buffers: MarchedBuffers): FloatBuffer {
  val illumination = illuminationOperator(dimensions)(1, mapOf(
      "depth" to buffers.depth,
      "position" to buffers.position,
      "normal" to buffers.normal
  )) as FloatBuffer

  val toned = toneMapping(dimensions)(2, mapOf(
      "input" to illumination
  )) as FloatBuffer

  return mixSceneOperator(dimensions)(3, mapOf(
      "color" to buffers.color,
      "illumination" to toned
  )) as FloatBuffer
}

fun normalizeDepthBuffer(near: Float, far: Float, input: FloatBuffer, output: FloatBuffer) {
  input.rewind()
  output.rewind()
  val inverseNear = 1 / near
  val bottom = (1 / far - inverseNear)

  for (i in 0 until input.capacity()) {
    val value = input.get()
    output.put((1 / value - inverseNear) / bottom)
  }
}
