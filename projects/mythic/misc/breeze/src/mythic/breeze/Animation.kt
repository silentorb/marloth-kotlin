package mythic.breeze

import mythic.spatial.*
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

fun areKeysValid(keys: Keyframes): Boolean =
    keys.map { it.time }.distinct().size == keys.size

data class AnimationChannel(
    val target: ChannelTarget,
    val keys: Keyframes
) {
  init {
    assert(areKeysValid(keys))
  }
}

data class Animation(
    val name: String,
    val duration: Float,
    val channels: List<AnimationChannel>,
    val channelMap: ChannelTypeMap
)

typealias Bones = List<Bone>

typealias Transformer = (bones: Bones, bone: Bone) -> Matrix

data class Bone(
    val name: String,
    val translation: Vector3,
    val rotation: Quaternion,
    val length: Float,
    val index: Int,
    val parent: Int = -1
)

data class VertexWeight(
    val index: Int,
    val strength: Float
)

typealias VertexWeights = Pair<VertexWeight, VertexWeight>

typealias WeightMap = Map<Vector3, VertexWeights>

data class ArrangedBoneNode(
    val index: Int,
    val parent: Int
)

typealias ChannelMap = Map<Int, AnimationChannel>
typealias ChannelTypeMap = Map<ChannelType, ChannelMap>

typealias MatrixSource = (index: Int) -> Matrix

fun mapChannels(channels: List<AnimationChannel>): ChannelTypeMap =
    channels
        .groupBy { it.target.type }
        .mapValues { c -> c.value.associate { Pair(it.target.boneIndex, it) } }

typealias ValueSource<T> = (boneIndex: Int) -> T?

fun staticMatrixSource(bones: Bones): MatrixSource = { i ->
  val bone = bones[i]
  transformBone(bone.translation, bone.rotation)
}

//fun transformSkeleton(armature: Armature, translationMap: ValueSource<Vector3>, rotationMap: ValueSource<Quaternion>): List<Matrix> {
fun transformSkeleton(bones: Bones, matrixSource: MatrixSource = staticMatrixSource(bones)): List<Matrix> {
  val init = Matrix()
  val result = Array(bones.size, { init })
  for (i in 0 until bones.size) {
    val bone = bones[i]
    val transform = matrixSource(i)
    result[i] = if (bone.parent == -1)
      transform
    else
      result[bone.parent] * transform
  }

  return result.toList()
}

fun transformAnimatedSkeleton(bones: List<Bone>, animation: Animation, timeElapsed: Float): List<Matrix> {
  val translationMap = animatedValueSource<Vector3>(animation.channelMap[ChannelType.translation], timeElapsed)
  val rotationMap = animatedValueSource<Quaternion>(animation.channelMap[ChannelType.rotation], timeElapsed)
    val matrixSource: MatrixSource = { i ->
    val bone = bones[i]
    val translation = translationMap(i) ?: bone.translation
    val rotation = rotationMap(i) ?: bone.rotation
    transformBone(translation, rotation)
  }
  return transformSkeleton(bones, matrixSource)
}

fun transformBone(translation: Vector3, rotation: Quaternion) =
    Matrix()
        .translate(translation)
        .rotate(rotation)


fun <T> emptyValueSource(): ValueSource<T> = { null }

fun <T> animatedValueSource(channelMap: ChannelMap?, timePassed: Float): ValueSource<T> =
    if (channelMap == null)
      emptyValueSource()
    else
      { i ->
        val channel = channelMap[i]
        if (channel != null)
          getChannelValue(channel, timePassed) as T
        else
          null
      }

fun projectBoneTail(matrix: Matrix, bone: Bone) =
//    Matrix().translate(Vector3(bone.length, 0f, 0f)).mul(matrix)
    Matrix(matrix).translate(bone.length, 0f, 0f)


inline fun getCurrentKeys(keys: Keyframes, timePassed: Float): Pair<Keyframe, Keyframe?> {
  for (i in 0 until keys.size) {
    val key = keys[i]
    if (key.time > timePassed) {
      return Pair(keys[i - 1], key)
    }
  }

  return Pair(keys.last(), null)
}

fun getChannelValue(channel: AnimationChannel, timePassed: Float): Any {
  val (firstKey, secondKey) = getCurrentKeys(channel.keys, timePassed)
  return if (secondKey == null) {
    when (channel.target.type) {
      ChannelType.rotation -> firstKey.value as Quaternion
      ChannelType.translation -> firstKey.value as Vector3
      else -> throw Error("Not implemented.")
    }
  } else {
    val duration = secondKey.time - firstKey.time
    val localSecondsPassed = timePassed - firstKey.time
    val progress = localSecondsPassed / duration
    when (channel.target.type) {
      ChannelType.rotation -> {
        val a = firstKey.value as Quaternion
        val b = secondKey.value as Quaternion
        val value = Quaternion(a).slerp(b, progress)
        value
      }
      ChannelType.translation -> {
        val a = firstKey.value as Vector3
        val b = secondKey.value as Vector3
        Vector3(Vector3m(a).lerp(Vector3m(b), progress))
      }

      else -> throw Error("Not implemented.")
    }
  }
}

fun getBoneIndex(bones: Bones, name: String): Int =
    bones.first { it.name == name }.index

fun getBone(bones: Bones, name: String): Bone =
    bones.first { it.name == name }

fun refineKeyframes(keys: Keyframes): Keyframes =
    if (keys.first().time == 0f)
      keys
    else
      listOf(keys.first().copy(time = 0f))
          .plus(keys)

fun shift(timeOffset: Float, duration: Float, keys: Keyframes): Keyframes =
    if (timeOffset == 0f)
      keys
    else {
      val (first, second) = keys.filter { it.time < duration }.partition { it.time + timeOffset > duration }
      val result = first.map { it.copy(time = it.time + timeOffset - duration) }
          .plus(second.map { it.copy(time = it.time + timeOffset) })
      listOf(result.last().copy(time = 0f)).plus(result)
    }

fun keySequence(offset: Vector3, increment: Float, values: List<Vector3>): Keyframes =
    values.mapIndexed { index, value ->
      Keyframe(increment * index, offset + value)
    }

fun keySequenceRotation(offset: Quaternion, increment: Float, values: List<Quaternion>): Keyframes =
    values.mapIndexed { index, value ->
      Keyframe(increment * index, offset * value)
    }