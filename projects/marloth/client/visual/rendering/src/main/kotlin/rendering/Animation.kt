package rendering

import mythic.spatial.Quaternion
import mythic.spatial.Vector3

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

data class Bone(
    val translation: Vector3,
    val rotation: Quaternion,
    val name: String
)

data class Armature(
    val bones: List<Bone>,
    val animations: List<Animation>
)
