package silentorb.metaview.common

import silentorb.metaview.common.views.ValueDisplayMap
import silentorb.metaview.common.views.newImage
import mythic.imaging.floatTextureToBytes
import mythic.imaging.grayscaleTextureToBytes
import org.joml.Vector2i
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer

val defaultBitmap: (Int) -> FloatBuffer = { length ->
  BufferUtils.createFloatBuffer(length * length * 3)
}

val defaultGrayscale: (Int) -> FloatBuffer = { length ->
  BufferUtils.createFloatBuffer(length * length)
}

fun fillerTypeValues(length: Int): ValueMap = mapOf(
    bitmapType to defaultBitmap,
    grayscaleType to defaultGrayscale
).mapValues { { it.value(length) } }

fun textureValueDisplays(dimensions: Vector2i): ValueDisplayMap = mapOf(
    bitmapType to ::floatTextureToBytes,
    grayscaleType to ::grayscaleTextureToBytes
).mapValues { (_, toBytes) ->
  { value: Any ->
    val buffer = value as FloatBuffer
    newImage(dimensions, toBytes(buffer))
  }
}