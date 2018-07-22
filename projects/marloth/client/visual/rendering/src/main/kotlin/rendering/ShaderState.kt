package rendering

import mythic.breeze.Bones
import mythic.spatial.Matrix
import mythic.spatial.putMatrix
import mythic.spatial.putVector3
import mythic.spatial.putVector4
import org.joml.times
import org.lwjgl.BufferUtils
import scenery.Light
import scenery.Scene
import java.nio.ByteBuffer

const val sizeOfFloat = 4
const val sizeOfMatrix = 16 * sizeOfFloat

fun padBuffer(buffer: ByteBuffer, count: Int) {
  for (i in 0 until count) {
    buffer.putFloat(0f)
  }
}

class BufferCustodian(val buffer: ByteBuffer) {
  private var bufferInitialized = false

  fun finish() {
    if (!bufferInitialized) {
      while (buffer.position() != buffer.capacity()) {
        buffer.put(1)
      }
      bufferInitialized = true
    }
//    buffer.position(buffer.capacity() - 1)
    buffer.rewind()
    assert(buffer.limit() == buffer.capacity())
  }
}

private const val headerSize = 4 * sizeOfFloat
private const val lightSize = 16 * sizeOfFloat
private const val maxLights = 40
const val sectionBufferSize = headerSize + lightSize * maxLights
private val sectionMemoryBuffer = BufferUtils.createByteBuffer(sectionBufferSize)
private val sectionBufferCustodian = BufferCustodian(sectionMemoryBuffer)

fun createLightBuffer(lights: List<Light>): ByteBuffer {
//  val lightSize = 12

//  val totalSize = sectionBufferSize
  val buffer = sectionMemoryBuffer

//  buffer.limit(sectionBufferSize)
  buffer.putInt(lights.size)
  padBuffer(buffer, 3)
  for (light in lights) {
    buffer.putInt(light.type.value)
    padBuffer(buffer, 3)
    buffer.putVector4(light.color)
    buffer.putVector3(light.position)
    padBuffer(buffer, 1)
    buffer.putVector4(light.direction)
  }
  sectionBufferCustodian.finish()
  return buffer
}

const val maxBoneCount = 128
const val boneBufferSize = maxBoneCount * sizeOfMatrix
private val boneMemoryBuffer = BufferUtils.createByteBuffer(boneBufferSize)
private val boneBufferCustodian = BufferCustodian(boneMemoryBuffer)

fun createBoneTransformBuffer(bones: Bones): ByteBuffer {
//  boneMemoryBuffer.rewind()
  for (bone in bones) {
    val newTransform = bone.transform(bones, bone)
    val diff = Matrix(newTransform) * Matrix(bone.restingTransform).invert()
    diff.get(boneMemoryBuffer)
    boneMemoryBuffer.position(boneMemoryBuffer.position() + sizeOfMatrix)
  }
  boneBufferCustodian.finish()
  return boneMemoryBuffer
}

const val sceneBufferSize = sizeOfMatrix + sizeOfFloat * 4 // One extra byte for padding
private val sceneMemoryBuffer = BufferUtils.createByteBuffer(sceneBufferSize)
private val sceneBufferCustodian = BufferCustodian(sceneMemoryBuffer)

fun createSceneBuffer(effectsData: EffectsData): ByteBuffer {
//  for (bone in bones) {
//    val newTransform = bone.transform(bones, bone)
//    val diff = Matrix(newTransform) * Matrix(bone.restingTransform).invert()
//    diff.get(boneBuffer)
//    boneBuffer.position(boneBuffer.position() + sizeOfMatrix)
//  }
  sceneMemoryBuffer.putMatrix(effectsData.camera.transform)
  sceneMemoryBuffer.putVector3(effectsData.camera.direction)
  sceneBufferCustodian.finish()
  return sceneMemoryBuffer
}
