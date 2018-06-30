package rigging

import mythic.breeze.*
import mythic.spatial.*
import org.joml.minus
import org.joml.plus

fun createSkeleton(): Bones {

  val base = BoneDefinition(
      name = "base",
      translation = Vector3(0f, 0f, 0.4f),
      tail = Vector3(0f, 0f, 0f),
      transform = independentTransform
  )

  val sternum = BoneDefinition(
      name = "sternum",
      tail = Vector3(0f, 0f, 0.3f),
      parent = base,
      transform = dependentTransform
  )

  val neck = BoneDefinition(
      name = "neck",
      tail = Vector3(0f, 0f, 0.05f),
      parent = sternum,
      transform = dependentTransform
  )

  val head = BoneDefinition(
      name = "head",
      tail = Vector3(0f, 0f, 0.15f),
      parent = neck,
      transform = dependentTransform
  )

  fun createSkeletonSide(suffix: String, mod: Float): List<BoneDefinition> {
    val upperOffset = 0.1f * mod
    val shoulder = BoneDefinition(
        name = "shoulder" + suffix,
        tail = Vector3(upperOffset, 0f, 0f),
        parent = sternum,
        transform = dependentTransform
    )
    val upperArm = BoneDefinition(
        name = "upperArm" + suffix,
        tail = Vector3(0f, 0f, -0.15f),
        parent = shoulder,
        transform = inverseKinematicJointTransform(Vector3(0f, 1f, 0f))
    )
    val foreArm = BoneDefinition(
        name = "foreArm" + suffix,
        tail = Vector3(0f, 0f, -0.15f),
        parent = upperArm,
        transform = pointAtChildTransform
    )
    val hand = BoneDefinition(
        name = "hand" + suffix,
        tail = Vector3(0f, 0f, -0.05f),
        parent = foreArm,
        transform = independentTransform,
        isGlobal = true
    )
    val lowerOffset = 0.05f * mod
    val hip = BoneDefinition(
        name = "hip" + suffix,
        tail = Vector3(lowerOffset, 0f, 0f),
        parent = base,
        transform = dependentTransform
    )
    val thigh = BoneDefinition(
        name = "thigh" + suffix,
        tail = Vector3(0f, 0f, -0.2f),
        parent = hip,
        transform = inverseKinematicJointTransform(Vector3(0f, -1f, 0f))
    )
    val shin = BoneDefinition(
        name = "shin" + suffix,
        tail = Vector3(0f, 0f, -0.2f),
        parent = thigh,
        transform = pointAtChildTransform
    )
    val foot = BoneDefinition(
        name = "foot" + suffix,
        tail = Vector3(0f, -0.1f, 0f),
        parent = shin,
        transform = independentTransform,
        isGlobal = true
    )
    return listOf(
        shoulder,
        upperArm,
        foreArm,
        hand,

        hip,
        thigh,
        shin,
        foot
    )
  }

  val rightBones = createSkeletonSide("R", 1f)
  val leftBones = createSkeletonSide("L", -1f)

  val bones = listOf(
      base,
      sternum,
      neck,
      head
  )
      .plus(leftBones)
      .plus(rightBones)

  return finalizeSkeleton(bones)
}

fun walkingAnimationSide(bones: Bones, duration: Float, suffix: String, timeOffset: Float): List<AnimationChannel> {
  val foot = getBone(bones, "foot" + suffix)
  val wrist = getBone(bones, "hand" + suffix)
  val division = duration / 4f
  return listOf(
      AnimationChannel(
          target = ChannelTarget(
              boneIndex = foot.index,
              type = ChannelType.translation
          ),
          keys = shift(timeOffset, duration, keySequence(foot.translation, division, listOf(
              Vector3(0f, 0.1f, 0f),
              Vector3(0f, 0f, 0.15f),
              Vector3(0f, -0.1f, 0f),
              Vector3(0f, 0f, 0f),
              Vector3(0f, 0.1f, 0f)
          )))
      ),
      AnimationChannel(
          target = ChannelTarget(
              boneIndex = wrist.index,
              type = ChannelType.translation
          ),
          keys = shift(timeOffset, duration, keySequence(wrist.translation, division, listOf(
              Vector3(0f, -0.1f, 0f),
              Vector3(0f, 0f, 0f),
              Vector3(0f, 0.1f, 0f),
              Vector3(0f, 0f, 0f),
              Vector3(0f, -0.1f, 0f)
          )))
      )
  )
}

fun walkingAnimation(bones: Bones): Animation {
  val duration = 1.5f
  val division = duration / 4f
  val base = getBone(bones, "base")
  val head = getBone(bones, "head")
  return Animation(
      duration = duration,
      channels =
      listOf(
          AnimationChannel(
              target = ChannelTarget(
                  boneIndex = base.index,
                  type = ChannelType.translation
              ),
              keys = keySequence(base.translation, division, listOf(
                  Vector3(0f, 0f, -0.06f),
                  Vector3(0f, 0f, -0.003f),
                  Vector3(0f, 0f, -0.06f),
                  Vector3(0f, 0f, -0.003f),
                  Vector3(0f, 0f, -0.06f)
              ))
          )
//          AnimationChannel(
//              target = ChannelTarget(
//                  boneIndex = head.index,
//                  type = ChannelType.rotation
//              ),
//              keys = listOf(
//                  Keyframe(0f, Quaternion().rotateY(Pi / 4f)),
//                  Keyframe(duration, Quaternion().rotateY(-Pi / 4f))
//              )
//          )
      )
          .plus(walkingAnimationSide(bones, duration, "R", 0f))
          .plus(walkingAnimationSide(bones, duration, "L", duration / 2f))
  )
}

fun humanAnimations(bones: Bones): List<Animation> {
  val duration = 4f
  return listOf(
      walkingAnimation(bones),
      Animation(
          duration = duration,
          channels = listOf(
              AnimationChannel(
                  target = ChannelTarget(
                      boneIndex = getBoneIndex(bones, "base"),
                      type = ChannelType.rotation
                  ),
                  keys = listOf(
                      Keyframe(0f, Quaternion().rotateZ(Pi / 2f)),
                      Keyframe(duration, Quaternion().rotateZ(-Pi / 2f))
                  )
              )
          )
      )
  )
}