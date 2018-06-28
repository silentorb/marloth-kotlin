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
//      applyAnimation(animation, bones, animationOffset)
      return bones
    }
  }
  return armature.bones
}

fun drawSkeleton(renderer: SceneRenderer, bones: Bones, modelTransform: Matrix) {
  for (bone in bones.filter { it.length > 0 }.drop(5).take(1)) {
    val parent = bone.parent
    if (parent == null)
      continue

    val head = modelTransform * bone.transform(bones, bone)
    val tail = projectBoneTail(head, bone)
    val a = Vector3().transform(head)
    val b = Vector3().transform(tail)
    renderer.drawLine(a, b, white, 2f)
  }
}
