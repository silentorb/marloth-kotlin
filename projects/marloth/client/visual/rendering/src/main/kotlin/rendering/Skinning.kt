package rendering

import mythic.breeze.Bones
import mythic.spatial.*
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

const val maxBoneCount = 128
const val sizeOfMatrix = 16 * 4
private val buffer = BufferUtils.createByteBuffer(maxBoneCount * sizeOfMatrix)

fun createBoneTransformBuffer(bones: Bones, originalBones: Bones): ByteBuffer {
  for (bone in bones) {

    val newTransform = bone.transform(bones, bone)
//    val original = originalBones[bone.index]
//    val originalTransform = original.transform(originalBones, original)
    var diff = Matrix(newTransform) * Matrix(bone.restingTransform).invert()
//    if (bone.name == "head") {
//      val position = Vector3(0f, 0.03f, 0.75f)
//      val k = position.transform(diff)
//      val at = Matrix().translate(Vector3(0f, 0f, 3f))
//          .rotate(Quaternion().rotateY(-Pi / 2f))
//      val ap = Vector3(0f, 0f, 6f)
//      val bt = Matrix().translate(Vector3(0f, 0f, 3f))
//      val invert = Matrix().rotate(Quaternion().rotateY(Pi / 2f))
//          .translate(Vector3(0f, 0f, -3f))
//      val invertAttempt = Matrix(at).invert()
//      val ct = Matrix().translate(Vector3(0f, 0f, 3f)) * invertAttempt
//
////          .rotate(Quaternion().rotateY(Pi / 2f))
////      val attempt =
//      val bp1 = ap.transform(bt)
//      val bp2 = ap.transform(Matrix(bt) * invertAttempt)
//      val cp1 = ap.transform(ct)
////      val maybe =
//      val b = 0
//    } else {
////      diff = Matrix()
//    }
    diff.get(buffer)
    buffer.position(buffer.position() + sizeOfMatrix)
  }
  buffer.flip()
  return buffer
}
