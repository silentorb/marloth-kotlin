package lab.views.shared

import lab.utility.white
import mythic.breeze.*
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.transform
import org.joml.times
import rendering.*

fun getAnimatedBones(armature: Armature, animationOffset: Float): Bones {
  if (armature.animations.any()) {
    val animation = armature.animations.first()
    // When switching animations the animation offset may be briefly greater than the duration
    if (animationOffset < animation.duration) {
      val bones = copyBones(armature.bones)
      applyAnimation(animation, bones, animationOffset)
      return bones
    }
  }
  return armature.bones
}

fun drawSkeleton(renderer: SceneRenderer, bones: Bones, modelTransform: Matrix) {
  for (bone in bones.filter { it.length > 0 }.take(1)) {
    val parent = bone.parent
    if (parent == null)
      continue

    val head = modelTransform * bone.transform(bones, bone)
//    val tail = head
//        .translate(Vector3(bone.length, 0f, 0f))
//    val tail = Matrix().translate(Vector3(bone.length, 0f, 0f)).mul(head)
    val tail = projectBoneTail(head, bone)
    renderer.drawLine(Vector3().transform(head), Vector3().transform(tail), white, 2f)
  }
}
