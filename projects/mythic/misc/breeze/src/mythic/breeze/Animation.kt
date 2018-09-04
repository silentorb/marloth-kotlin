package mythic.breeze

import mythic.spatial.*
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
    val translation: Vector3m = Vector3m(),
    val parent: BoneDefinition? = null,
    val transform: Transformer,
    val tail: Vector3m,
    val isGlobal: Boolean = false
)

data class Bone(
    val name: String,
    var translation: Vector3m,
    var rotation: Quaternion,
    var length: Float,
    val index: Int,
    val transform: Transformer,
    var restingTransform: Matrix = Matrix(),
    var parent: Bone? = null,
    val isGlobal: Boolean
)

data class VertexWeight(
    val index: Int,
    val strength: Float
)

typealias VertexWeights = Pair<VertexWeight, VertexWeight>

typealias WeightMap = Map<Vector3m, VertexWeights>

data class Armature(
    val bones: Bones,
    val originalBones: Bones,
    val animations: List<Animation>
)

//private fun getBoneDefinitionTranslation(bone: BoneDefinition): Vector3m {
//  val translation = bone.translation + bone.tail
//  return if (bone.parent == null)
//    translation
//  else
//    getBoneDefinitionTranslation(bone.parent) + translation
//}

fun getBoneTranslation(bones: Bones, bone: Bone): Vector3m =
    transformVector(bone.transform(bones, bone))

fun transformBone(translation: Vector3m, rotation: Quaternion, length: Float) =
    Matrix()
        .translate(translation)
        .rotate(rotation)
//        .translate(Vector3m(length, 0f, 0f))

fun transformBone(bone: Bone) =
    transformBone(bone.translation, bone.rotation, bone.length)

fun getSimpleBoneTransform(bone: Bone): Matrix {
  if (bone.isGlobal)
    return transformBone(bone)

  val parent = bone.parent
  val parentTransform = if (parent == null)
    Matrix()
  else
    projectBoneTail(getSimpleBoneTransform(parent), parent)

  return parentTransform * transformBone(bone)
}

fun getBoneLineage(bone: Bone): List<Bone> {
  val parent = bone.parent
  return if (parent == null)
    listOf(bone)
  else
    getBoneLineage(parent).plus(bone)
}

fun getSimpleBoneTransform2(bone: Bone): Matrix {
  val lineage = getBoneLineage(bone)
  val transforms = lineage.map { projectBoneTail(transformBone(it), it) }
  return transforms.reduce { a, b -> a * b }
}

fun getSimpleBoneTranslation(bone: Bone): Vector3m {
  val transform = getSimpleBoneTransform2(bone)
  val translation = transformVector(transform)
  return translation
}

val independentTransform: Transformer = { bones, bone ->
  transformBone(bone.translation, bone.rotation, bone.length)
}

fun projectBoneTail(matrix: Matrix, bone: Bone) =
//    Matrix().translate(Vector3m(bone.length, 0f, 0f)).mul(matrix)
    Matrix(matrix).translate(bone.length, 0f, 0f)

val dependentTransform: Transformer = { bones, bone ->
  val parent = bone.parent
  val parentTransform = if (parent == null)
    Matrix()
  else
    projectBoneTail(parent.transform(bones, parent), parent)

  parentTransform * transformBone(bone.translation, bone.rotation, bone.length)
}

fun inverseKinematicJointTransform(outVector: Vector3m): Transformer = { bones, bone ->
  //  val previousBone = bones[bone.index - 1]
  val endBone = bones[bone.index + 2]
  val transform = dependentTransform(bones, bone)
  val a = transformVector(transform)
  val b = getBoneTranslation(bones, endBone)
  val middle = (a + b) / 2f
  val a2 = (a - b).length() / 2f
  val c2 = bone.length
  val projectLength = Math.sqrt((c2 * c2 - a2 * a2).toDouble()).toFloat()
  val defaultTail = transformVector(projectBoneTail(transform, bone)) - a
  val newTail = (middle + outVector * projectLength) - a
  val tail = middle + outVector * projectLength - a
//  val translation = a
  val j = projectBoneTail(projectBoneTail(bone.parent!!.transform(bones, bone.parent!!), bone.parent!!), bone.parent!!)
  val i = transformVector(j)
  val k = i - a
  val rotation = Quaternion().rotateTo(k, newTail)// rotateToward(translation - tail)
//  transformBone(translation, rotation, bone.length)
  transform * Matrix().rotate(rotation)
//  transform*Matrix().rotate(Quaternion().rotateZ(Pi / 4))
//  transform
  transformBone(a, rotateToward(tail), bone.length)
//  projectBoneTail(bone.parent!!.transform(bones, bone.parent!!), bone.parent!!) * transformBone(bone.translation, rotation, bone.length)
}

val pointAtChildTransform: Transformer = { bones, bone ->
  val transform = dependentTransform(bones, bone)
  val nextBone = bones[bone.index + 1]
  val base = transformVector(transform)
  val target = getBoneTranslation(bones, nextBone) - base
//  val tail = transformVector(projectBoneTail(transform, bone)) - base
//  transform.rotate(Quaternion().rotateTo(tail, target - base))
  transformBone(base, rotateToward(target), bone.length)
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
    Bone(
        index = index,
        name = it.name,
        translation = it.translation,
        rotation = Quaternion(),
        transform = it.transform,
        length = it.tail.length(),
        isGlobal = it.isGlobal
    )
  }

  val definitions = boneDefinitions.iterator()

  for (bone in bones) {
    val oldBone = definitions.next()
    if (oldBone.parent != null) {
      val parent = bones[boneDefinitions.indexOf(oldBone.parent)]
      bone.parent = parent

      if (oldBone.isGlobal) {
        bone.translation += getSimpleBoneTranslation(parent)
        bone.rotation = rotateToward(oldBone.tail)
      } else {
        val rotation = Quaternion(cumulativeRotation(parent)).invert() * rotateToward(oldBone.tail)
        bone.rotation = rotation
      }
      bone.restingTransform = getSimpleBoneTransform(bone)
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
      ChannelType.translation -> firstKey.value as Vector3m
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
        val a = firstKey.value as Vector3m
        val b = secondKey.value as Vector3m
        Vector3m(a).lerp(b, progress)
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
        bone.translation = value as Vector3m
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

fun keySequence(offset: Vector3m, increment: Float, values: List<Vector3m>): Keyframes =
    values.mapIndexed { index, value ->
      Keyframe(increment * index, offset + value)
    }

fun keySequenceRotation(offset: Quaternion, increment: Float, values: List<Quaternion>): Keyframes =
    values.mapIndexed { index, value ->
      Keyframe(increment * index, offset * value)
    }