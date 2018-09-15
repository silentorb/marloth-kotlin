package rendering.meshes.loading

import com.fasterxml.jackson.annotation.JsonCreator
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import rendering.meshes.AttributeName


enum class AccessorType {
  SCALAR,
  MAT4,
  VEC2,
  VEC3,
  VEC4
}

enum class ComponentType(val value: Int) {
  Byte(5120),
  UnsignedByte(5121),
  Short(5122),
  UnsignedShort(5123),
  UnsignedInt(5125),
  Float(5126);

  companion object {
    @JsonCreator
    fun forValue(value: String) =
        ComponentType.values().first { it.value.toString() == value }
  }
}

enum class AttributeType {
  COLOR_0,
  JOINTS_0,
  JOINTS_1,
  JOINTS_2,
  NORMAL,
  POSITION,
  TANGENT,
  TEXCOORD_0,
  WEIGHTS_0,
  WEIGHTS_1,
  WEIGHTS_2
}

val attributeMap = mapOf(
    AttributeType.NORMAL to AttributeName.normal,
    AttributeType.POSITION to AttributeName.position,
    AttributeType.TEXCOORD_0 to AttributeName.uv
)

val attributeMap2 = mapOf(
    AttributeName.normal to AttributeType.NORMAL,
    AttributeName.position to AttributeType.POSITION,
    AttributeName.uv to AttributeType.TEXCOORD_0
)

data class Accessor(
    val bufferView: Int,
    val byteOffset: Int,
    val componentType: Int,
    val count: Int,
    val type: AccessorType,
    val name: String?
)

data class BufferView(
    val buffer: Int,
    val byteLength: Int,
    val byteOffset: Int,
    val byteStride: Int,
    val target: Int,
    val name: String?
)

data class Node(
    val name: String,
    val children: List<Int>?,
    val rotation: Vector4?,
    val translation: Vector3?
)

data class BufferInfo(
    val byteLength: Int,
    val uri: String
)

data class Primitive(
    val attributes: Map<AttributeType, Int>,
    val indices: Int,
    val material: Int
)

data class MeshInfo2(
    val primitives: List<Primitive>,
    val name: String
)

data class TextureReference(
    val index: Int
)

data class Metallic(
    val baseColorFactor: Vector4,
    val metallicFactor: Float,
    val baseColorTexture: TextureReference?
)

data class MaterialInfo(
    val pbrMetallicRoughness: Metallic,
    val emissiveFactor: List<Float>?
)

data class Image(
    val uri: String
)

data class Texture(
    val source: Int
)

data class Skin(
    val inverseBindMatrices: Int,
    val joints: List<Int>,
    val skeleton: Int
)

data class ChannelTarget(
    val node: Int,
    val path: String
)

data class AnimationChannel(
    val sampler: Int,
    val target: ChannelTarget
)

data class AnimationSampler(
    val input: Int,
    val interpolation: String,
    val output: Int
)

data class IndexedAnimation(
    val name: String,
    val channels: List<AnimationChannel>,
    val samplers: List<AnimationSampler>
)

data class GltfInfo(
    val accessors: List<Accessor>,
    val animations: List<IndexedAnimation>?,
    val bufferViews: List<BufferView>,
    val buffers: List<BufferInfo>,
    val images: List<Image>?,
    val meshes: List<MeshInfo2>,
    val materials: List<MaterialInfo>,
    val nodes: List<Node>,
    val skins: List<Skin>?,
    val textures: List<Texture>?
)
