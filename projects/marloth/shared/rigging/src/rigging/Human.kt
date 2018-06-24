package rigging

import mythic.breeze.*
import mythic.spatial.*
import org.joml.minus
import org.joml.plus

fun kneeTransform(boneLength: Float, outVector: Vector3): Transformer = { bones, bone ->
  val previousBone = bones[bone.index - 1]
  val nextBone = bones[bone.index + 1]
  val a = getBoneTranslation(bones, previousBone)
  val b = getBoneTranslation(bones, nextBone)
  val middle = (a + b) / 2f
  val normal = (middle - a).cross(outVector).normalize()
  val a2 = (a - b).length() / 2f
  val c2 = boneLength
//  val (a2, c2) = flattenPoints(normal, listOf(a, middle))
  val projectLength = Math.sqrt((c2 * c2 - a2 * a2).toDouble()).toFloat()
  val position = middle + outVector * projectLength
  Matrix()
      .translate(position)
      .rotate(bone.rotation)
}

fun createSkeleton(): Bones {

  val base = Bone(
      name = "base",
      translation = Vector3(0f, 0f, 0.4f),
      transform = independentTransform
  )

  val sternum = Bone(
      name = "sternum",
      translation = Vector3(0f, 0f, 0.3f),
      parent = base,
      transform = dependentTransform
  )

  val neck = Bone(
      name = "neck",
      translation = Vector3(0f, 0f, 0.05f),
      parent = sternum,
      transform = dependentTransform
  )

  val head = Bone(
      name = "head",
      translation = Vector3(0f, 0f, 0.15f),
      parent = neck,
      transform = dependentTransform
  )

  fun createSkeletonSide(suffix: String, mod: Float): List<Bone> {
    val upperOffset = 0.1f * mod
    val shoulder = Bone(
        name = "shoulder" + suffix,
        translation = Vector3(upperOffset, 0f, 0f),
        parent = sternum,
        transform = dependentTransform
    )
    val elbow = Bone(
        name = "elbow" + suffix,
        translation = getBoneTranslation(listOf(), shoulder) + Vector3(0f, 0f, -0.15f),
        parent = shoulder,
        transform = kneeTransform(0.15f, Vector3(0f, 1f, 0f))
    )
    val wrist = Bone(
        name = "wrist" + suffix,
        translation = getBoneTranslation(listOf(), shoulder) + Vector3(0f, 0f, -0.15f * 2),
        parent = elbow,
        transform = independentTransform
    )
    val hand = Bone(
        name = "hand" + suffix,
        translation = Vector3(0f, 0f, -0.05f),
        parent = wrist,
        transform = dependentTransform
    )
    val lowerOffset = 0.05f * mod
    val hip = Bone(
        name = "hip" + suffix,
        translation = Vector3(lowerOffset, 0f, 0f),
        parent = base,
        transform = dependentTransform
    )
    val knee = Bone(
        name = "knee" + suffix,
        translation = Vector3(lowerOffset, 0f, 0.2f),
        parent = hip,
        transform = kneeTransform(0.2f, Vector3(0f, -1f, 0f))
    )
    val ankle = Bone(
        name = "foot" + suffix,
        translation = Vector3(lowerOffset, 0f, 0f),
        parent = knee,
        transform = independentTransform
    )
    val toes = Bone(
        name = "toes" + suffix,
        translation = Vector3(0f, -0.1f, 0f),
        parent = ankle,
        transform = dependentTransform
    )
    return listOf(
        shoulder,
        elbow,
        wrist,
        hand,

        hip,
        knee,
        ankle,
        toes
    )
  }

  val rightBones = createSkeletonSide("R", 1f)
  val leftBones = createSkeletonSide("L", -1f)

  val bones = listOf(
      base,
      sternum,
      head,
      neck
  )
      .plus(leftBones)
      .plus(rightBones)

  finalizeSkeleton(bones)
  return bones
}

fun walkingAnimationSide(bones: Bones, duration: Float, suffix: String, timeOffset: Float): List<AnimationChannel> {
  val foot = getBone(bones, "foot" + suffix)
  val wrist = getBone(bones, "wrist" + suffix)
  val division = duration / 4f
  return listOf(
      AnimationChannel(
          target = ChannelTarget(
              boneIndex = foot.index,
              type = ChannelType.translation
          ),
          keys = shift(timeOffset, duration, listOf(
              Keyframe(0f, foot.translation + Vector3(0f, 0.1f, 0f)),
              Keyframe(division, foot.translation + Vector3(0f, 0f, 0.15f)),
              Keyframe(division * 2, foot.translation + Vector3(0f, -0.1f, 0f)),
              Keyframe(division * 3, foot.translation + Vector3(0f, 0f, 0f)),
              Keyframe(division * 4, foot.translation + Vector3(0f, 0.1f, 0f))
          ))
      ),
      AnimationChannel(
          target = ChannelTarget(
              boneIndex = wrist.index,
              type = ChannelType.translation
          ),
          keys = shift(timeOffset, duration, listOf(
              Keyframe(0f, wrist.translation + Vector3(0f, -0.1f, 0f)),
              Keyframe(division, wrist.translation + Vector3(0f, 0f, 0f)),
              Keyframe(division * 2, wrist.translation + Vector3(0f, 0.1f, 0f)),
              Keyframe(division * 3, wrist.translation + Vector3(0f, 0f, 0f)),
              Keyframe(division * 4, wrist.translation + Vector3(0f, -0.1f, 0f))
          ))
      )
  )
}

fun walkingAnimation(bones: Bones): Animation {
  val duration = 1.5f
  val division = duration / 4f
  val base = getBone(bones, "base")
  return Animation(
      duration = duration,
      channels =
      listOf(
          AnimationChannel(
              target = ChannelTarget(
                  boneIndex = base.index,
                  type = ChannelType.translation
              ),
              keys = listOf(
                  Keyframe(0f, base.translation + Vector3(0f, 0f, -0.06f)),
                  Keyframe(division, base.translation + Vector3(0f, 0f, -0.003f)),
                  Keyframe(division * 2, base.translation + Vector3(0f, 0f, -0.06f)),
                  Keyframe(division * 3, base.translation + Vector3(0f, 0f, -0.003f)),
                  Keyframe(division * 4, base.translation + Vector3(0f, 0f, -0.06f))
              )
          )
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