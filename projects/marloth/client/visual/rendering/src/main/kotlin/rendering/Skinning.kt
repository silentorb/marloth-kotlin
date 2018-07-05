package rendering

import mythic.breeze.Bones
import mythic.spatial.*
import org.joml.times
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

const val maxBoneCount = 128
const val sizeOfMatrix = 16 * 4
const val boneBufferSize = maxBoneCount * sizeOfMatrix
private val buffer = BufferUtils.createByteBuffer(boneBufferSize)
private var bufferInitialized = false

fun createBoneTransformBuffer(bones: Bones): ByteBuffer {
  for (bone in bones) {
    val newTransform = bone.transform(bones, bone)
    var diff = Matrix(newTransform) * Matrix(bone.restingTransform).invert()
    diff.get(buffer)
    buffer.position(buffer.position() + sizeOfMatrix)
  }
  if (!bufferInitialized) {
    while (buffer.position() != boneBufferSize) {
      buffer.put(1)
    }
    bufferInitialized = true
  }
  buffer.flip()
  return buffer
}
