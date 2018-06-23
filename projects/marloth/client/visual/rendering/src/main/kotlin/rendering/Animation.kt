package rendering

import mythic.spatial.Matrix
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.times

data class ChannelTarget2(
    val bone: Bone,
    val type: ChannelType
)

enum class ChannelType {
  rotation,
  scale,
  translation
}

data class AnimationChannel2(
    val sampler: Keyframes,
    val target: ChannelTarget2
)

data class Keyframe(
    val time: Float,
    val value: Any
)

typealias Keyframes = List<Keyframe>

data class ChannelTarget(
    val boneIndex: Int,
    val type: ChannelType
)

data class AnimationOld(
    val channels: List<AnimationChannel2>,
    val samplers: List<Keyframes>
)

data class AnimationChannel(
    val target: ChannelTarget,
    val keys: Keyframes
)

data class Animation(
    val duration: Float,
    val channels: List<AnimationChannel>
)

typealias Bones = List<Bone>

data class Bone(
    val name: String,
    var rotation: Quaternion = Quaternion(),
    var translation: Vector3,
    var parent: Bone? = null,
    var children: List<Bone> = listOf(),
    var index: Int = -1
)

data class Armature(
    val bones: Bones,
    val animations: List<Animation>
)

fun joinSkeletonChildren(bones: Bones) {
  for (bone in bones) {
    bone.children = bones.filter { it.parent == bone }
  }
}

fun finalizeSkeleton(bones: Bones) {
  joinSkeletonChildren(bones)
  bones.forEachIndexed { index, bone -> bone.index = index }
}

fun getBoneTransform(bone: Bone): Matrix {
  val parent = bone.parent
  return Matrix()
      .translate(bone.translation)
      .rotate(bone.rotation) *
      if (parent == null)
        Matrix()
      else
        getBoneTransform(parent)
}

fun copyBones(bones: Bones): Bones {
  val newBones = bones.map {
    it.copy(parent = null, children = listOf())
  }

  val oldBones = bones.iterator()

  for (bone in newBones) {
    val oldBone = oldBones.next()
    val parent = oldBone.parent
    if (parent != null) {
      bone.parent = newBones[parent.index]
    }
  }

  joinSkeletonChildren(newBones)

  return newBones
}

fun applyAnimation(animation: Animation, bones: Bones, timePassed: Float) {
  for (channel in animation.channels) {
    val bone = bones[channel.target.boneIndex]
    val firstKey = channel.keys[0]
    val secondKey = channel.keys[1]

    val duration = secondKey.time - firstKey.time
    val localSecondsPassed = timePassed - firstKey.time
    val progress = localSecondsPassed / duration
    when (channel.target.type) {
      ChannelType.rotation -> {
        val a = firstKey.value as Vector3
        val b = secondKey.value as Vector3
        bone.translation = Vector3(a).lerp(b, progress)
      }

      else -> throw Error("Not implemented.")
    }
  }
}