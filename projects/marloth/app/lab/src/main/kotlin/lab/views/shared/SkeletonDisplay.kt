package lab.views.shared

import lab.utility.white
import mythic.breeze.Armature
import mythic.breeze.Bones
import mythic.breeze.applyAnimation
import mythic.breeze.copyBones
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
  for (bone in bones) {
    val parent = bone.parent
    if (parent == null)
      continue

    val transform = modelTransform * bone.transform(bones, bone)
    val parentPosition = Vector3().transform(modelTransform * parent.transform(bones, parent))
    renderer.drawLine(parentPosition, Vector3().transform(transform), white, 2f)
  }
}
