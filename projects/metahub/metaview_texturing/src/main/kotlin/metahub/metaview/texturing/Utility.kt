package metahub.metaview.texturing

import metahub.metaview.common.ValueMap
import metahub.metaview.common.bitmapType
import metahub.metaview.common.grayscaleType
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
