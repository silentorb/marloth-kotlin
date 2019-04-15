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
//    // When switching animations the animation withOffset may be briefly greater than the duration
//    if (animationOffset < animation.duration) {
//      val bones = copyBones(armature.bones)
////      bones.first {it.name == "head"}.rotation *= Quaternion().rotateY(-Pi / 4f)
//      applyAnimation(animation, bones, animationOffset)
//      return bones
//    }
//  }
//  return armature.bones
//}
