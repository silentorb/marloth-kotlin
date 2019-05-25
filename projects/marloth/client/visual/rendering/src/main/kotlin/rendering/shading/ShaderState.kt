package rendering.shading

import mythic.glowing.BufferCustodian
import mythic.spatial.*
import org.joml.times
import org.lwjgl.BufferUtils
import rendering.EffectsData
import scenery.Light
import java.nio.ByteBuffer

const val sizeOfFloat = 4
const val sizeOfMatrix = 16 * sizeOfFloat

fun padBuffer(buffer: ByteBuffer, count: Int) {
  for (i in 0 until count) {
    buffer.putFloat(0f)
  }
}

private const val headerSize = 4 * sizeOfFloat
private const val lightSize = 16 * sizeOfFloat
private const val maxLights = 128
const val sectionBufferSize = headerSize + lightSize * maxLights
private val sectionMemoryBuffer = BufferUtils.createByteBuffer(sectionBufferSize)
private val sectionBufferCustodian = BufferCustodian(sectionMemoryBuffer)

data class LightingConfig(
    val ambient: Float = 0f
)

fun createLightBuffer(config: LightingConfig, lights: List<Light>): ByteBuffer {
  val buffer = sectionMemoryBuffer
  buffer.putInt(lights.size)
  buffer.putFloat(config.ambient)
  padBuffer(buffer, 2)
  for (light in lights) {
//    buffer.putInt(light.type.value)
    buffer.putInt(5)
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

fun createBoneTransformBuffer(originalTransforms: List<Matrix>, transforms: List<Matrix>): ByteBuffer {
  assert(transforms.size <= 128)

  val originalIterator = originalTransforms.iterator()
  transforms.forEachIndexed { i, transform ->
    val originalTransform = originalIterator.next()
    val diff = Matrix(transform) * Matrix(originalTransform).invert()
    diff.get(boneMemoryBuffer)
//    originalTransform.get(boneMemoryBuffer)
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
