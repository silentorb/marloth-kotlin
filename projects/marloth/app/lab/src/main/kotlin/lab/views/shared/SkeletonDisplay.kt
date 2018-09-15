package lab.views.shared

import lab.utility.white
import mythic.breeze.*
import mythic.spatial.*
import org.joml.times
import rendering.*
//
//fun getAnimatedBones(armature: Armature, animationOffset: Float): Bones {
//  if (armature.animations.any()) {
//    val animation = armature.animations.first()
//    // When switching animations the animation offset may be briefly greater than the duration
//    if (animationOffset < animation.duration) {
//      val bones = copyBones(armature.bones)
////      bones.first {it.name == "head"}.rotation *= Quaternion().rotateY(-Pi / 4f)
//      applyAnimation(animation, bones, animationOffset)
//      return bones
//    }
//  }
//  return armature.bones
//}

fun drawSkeleton(renderer: SceneRenderer, armature: Armature, transforms: List<Matrix>, modelTransform: Matrix) {
  armature.bones
      .filter { it.parent != -1 }
      .forEach { bone ->
        //    val parent = bone.parent
//    if (parent == null)
//      continue

//    val head = modelTransform * bone.transform(bones, bone)
//    val tail = projectBoneTail(head, bone)
        val a = Vector3().transform(transforms[bone.index])
        val b =  Vector3().transform(transforms[bone.parent])
        renderer.drawLine(a, b, white, 2f)
      }
}
