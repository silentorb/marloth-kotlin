package mythic.breeze

import mythic.spatial.*
import org.joml.minus
import org.joml.plus
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
    val duration: Float,
    val channels: List<AnimationChannel>
)

typealias Bones = List<Bone>

typealias Transformer = (bones: Bones, bone: Bone) -> Matrix

data class BoneDefinition(
    val name: String,
    val translation: Vector3 = Vector3(),
    val parent: BoneDefinition? = null,
    val transform: Transformer,
    val tail: Vector3
)

data class Bone(
    val name: String,
    var translation: Vector3,
    var rotation: Quaternion,
    var length: Float,
    val index: Int,
    val transform: Transformer,
    var parent: Bone? = null
)

data class VertexWeight(
    val index: Int,
    val strength: Float
)

typealias VertexWeights = Pair<VertexWeight, VertexWeight>

typealias WeightMap = Map<Vector3, VertexWeights>

data class Armature(
    val bones: Bones,
    val originalBones: Bones,
    val animations: List<Animation>
)

//private fun getBoneDefinitionTranslation(bone: BoneDefinition): Vector3 {
//  val translation = bone.translation + bone.tail
//  return if (bone.parent == null)
//    translation
//  else
//    getBoneDefinitionTranslation(bone.parent) + translation
//}

fun getBoneTranslation(bones: Bones, bone: Bone): Vector3 =
    Vector3().transform(bone.transform(bones, bone))

fun transformBone(matrix: Matrix, translation: Vector3, rotation: Quaternion, length: Float) =
    matrix
        .translate(translation)
        .rotate(rotation)
//        .translate(Vector3(length, 0f, 0f))

val independentTransform: Transformer = { bones, bone ->
  transformBone(Matrix(), bone.translation, bone.rotation, bone.length)
}

fun projectBoneTail(matrix: Matrix, bone: Bone) =
//    Matrix().translate(Vector3(bone.length, 0f, 0f)).mul(matrix)
    Matrix(matrix).translate(Vector3(bone.length, 0f, 0f))

val dependentTransform: Transformer = { bones, bone ->
  val parent = bone.parent
  val parentTransform = if (parent == null)
    Matrix()
  else
    projectBoneTail(parent.transform(bones, parent), parent)

  val matrix = Matrix()
      .mul(parentTransform)

  transformBone(matrix, bone.translation, bone.rotation, bone.length)
}

fun cumulativeRotation(bone: Bone): Quaternion {
  val parent = bone.parent
  return if (parent != null)
    cumulativeRotation(parent) * bone.rotation
  else
    bone.rotation
}

fun finalizeSkeleton(boneDefinitions: List<BoneDefinition>): Bones {
  val bones = boneDefinitions.mapIndexed { index, it ->
    //    val translation = getBoneDefinitionTranslation(it)
    val length = it.tail.length()
    Bone(
        index = index,
        name = it.name,
        translation = it.translation,
        rotation = Quaternion(),
        transform = it.transform,
        length = it.tail.length()
    )
  }

  val definitions = boneDefinitions.iterator()

  for (bone in bones) {
    val oldBone = definitions.next()
    val parent = oldBone.parent
    if (parent != null) {
      val parent = bones[boneDefinitions.indexOf(parent)]
      bone.parent = parent
      val rotation = Quaternion(cumulativeRotation(parent)).invert() * rotateToward(oldBone.tail)
      bone.rotation = rotation
    }
  }

  return bones
}

fun copyBones(bones: Bones): Bones {
  val newBones = bones.map {
    it.copy(parent = null)
  }

  val oldBones = bones.iterator()

  for (bone in newBones) {
    val oldBone = oldBones.next()
    val parent = oldBone.parent
    if (parent != null) {
      bone.parent = newBones[parent.index]
    }
  }

  return newBones
}

fun getCurrentKeys(keys: Keyframes, timePassed: Float): Pair<Keyframe, Keyframe?> {
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
        Vector3(a).lerp(b, progress)
      }

      else -> throw Error("Not implemented.")
    }
  }
}

fun applyAnimation(animation: Animation, bones: Bones, timePassed: Float) {
  for (channel in animation.channels) {
    val bone = bones[channel.target.boneIndex]

    val value = getChannelValue(channel, timePassed)
    when (channel.target.type) {
      ChannelType.rotation -> {
        bone.rotation = value as Quaternion
      }
      ChannelType.translation -> {
        bone.translation = value as Vector3
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