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

//data class AnimationSampler2(
//    val input: Int,
////    val interpolation: String,
//    val output: Int
//)

data class Animation(
    val channels: List<AnimationChannel2>,
    val samplers: List<Keyframes>
)

typealias Bones = List<Bone>

data class Bone(
    val name: String,
    val rotation: Quaternion = Quaternion(),
    val translation: Vector3,
    val parent: Bone? = null,
    var children: List<Bone> = listOf()
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

fun getBoneTransform(bone: Bone): Matrix =
    Matrix()
        .translate(bone.translation) *
//        .rotate(bone.rotation) *
        if (bone.parent == null)
          Matrix()
        else
          getBoneTransform(bone.parent)
