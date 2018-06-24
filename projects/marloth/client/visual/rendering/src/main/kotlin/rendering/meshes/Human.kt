package rendering.meshes

import mythic.sculpting.*
import mythic.spatial.*
import org.joml.minus
import org.joml.plus
import rendering.*

data class HeadPorts(
    val neck: Port
)

val neckTop = Vector2(0.06f, 0.9f)

fun headFrontPath() = transformVertices(Matrix().scale(0.8f).rotateX(Pi / 2), createArcXY(0.6f, 8, Pi).take(7))

fun bodySidePath() = convertAsXZ(listOf(
    neckTop.copy(),
    Vector2(0.1f, 0.75f),
    Vector2(0.32f, 0.7f),
    Vector2(0.42f, 0.5f),
    Vector2(0.42f, 0f)
))

fun bodyFrontPath() = convertAsXZ(listOf(
    neckTop.copy(),
    Vector2(0.1f, 0.75f),
    Vector2(0.2f, 0.6f),
    Vector2(0.25f, 0.4f),
    Vector2(0.25f, 0f)
))

fun createHead(resolution: Int): MeshNode<HeadPorts> {
  val mesh = FlexibleMesh()
  val headPath = headFrontPath()
//  val headPath = createArc(0.6f, 8, Pi).take(7)
//  transformVertices(Matrix().rotateY(-Pi / 2), headPath)
//  headPath.forEach { it.x *= 0.8f }
  lathe(mesh, headPath, 8 * resolution)
  val edge = mesh.edges.last().references.first()
  return MeshNode(mesh, HeadPorts(
      edge
  ), MeshInfo(listOf(), listOf(mapOf(edge to 1f))))
}

data class TorsoPorts(
    val neck: Port
)

fun createTorso(resolution: Int): MeshNode<TorsoPorts> {
  val mesh = FlexibleMesh()
  val bodyFront = bodyFrontPath()
  val bodySide = bodySidePath()
  latheTwoPaths(mesh, createLatheCourse(resolution), bodySide, bodyFront)
  val edge = mesh.edges[0].references[0].previous!!
  return MeshNode(mesh, TorsoPorts(
      edge
  ), MeshInfo(listOf(), listOf(mapOf(edge to 1f))))
}

fun createHumanMesh(): Pair<FlexibleMesh, MeshInfo> {
  val neck = 0.05f
  val head = createHead(3)
  val torso = createTorso(3)
  val mesh = joinMeshNodes(head.mesh, head.ports.neck, torso.mesh, torso.ports.neck)
  alignToFloor(mesh.distinctVertices, 0f)
  calculateNormals(mesh)
  return Pair(mesh, MeshInfo(listOf(), head.info.edgeGroups.plus(torso.info.edgeGroups)))
}

val createHumanOld: ModelGenerator = {
  val (mesh, info) = createHumanMesh()
  Model(
      mesh = mesh,
      info = info,
      groups = listOf(mapMaterialToMesh(Material(Vector4(0.3f, 0.25f, 0.0f, 1f)), mesh)
      ))
}

fun rotateZ(vertices: Vertices) =
    transformVertices(Matrix().rotateZ(Pi / 2), vertices)

fun joinPaths(first: Vertices, second: Vertices): Vertices {
  val gap = 0.1f
  val singleOffset = second.first().z - first.last().z + gap
  val offset = Vector3(0f, 0f, -singleOffset)
  val modifiedSecond = transformVertices(Matrix().translate(offset), second)
  return first.plus(modifiedSecond)
}

val createHuman: ModelGenerator = {
  val mesh = FlexibleMesh()
  val sidePath = joinPaths(headFrontPath(), bodySidePath())
  val frontPath = joinPaths(headFrontPath(), bodyFrontPath())
  latheTwoPaths(mesh, createLatheCourse(2, Pi), frontPath, sidePath)

  val k = mesh.distinctVertices.sortedBy { it.x }
  val originalFaces = mesh.faces.toList()
  val mirroredFaces = mirrorAlongY(mesh)

  mesh.createEdges(rotateZ(sidePath))
  mesh.createEdges(frontPath)

  val k2 = mesh.distinctVertices.sortedBy { it.x }
//  val k3 = mesh.distinctVertices2
//  mesh.createEdges(headFrontPath())
//  mesh.createEdges(rotateZ(bodySidePath()))
//  alignToFloor(mesh.distinctVertices, 0f)
  calculateNormals(mesh)

  Model(
      mesh = mesh,
      info = MeshInfo(),
      groups = listOf(
          MeshGroup(Material(Vector4(0.3f, 0.25f, 0.0f, 1f)), originalFaces, "Original"),
          MeshGroup(Material(Vector4(0.2f, 0.25f, 0.3f, 1f)), mirroredFaces, "Mirror")
      ))
}

enum class Side {
  left,
  right
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
        transform = independentTransform
    )
    val wrist = Bone(
        name = "wrist" + suffix,
        translation = getBoneTranslation(listOf(), elbow) + Vector3(0f, 0f, -0.15f),
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
        transform = kneeTransform(0.2f)
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
      )
  )
}

fun walkingAnimation(bones: Bones): Animation {
  val duration = 3f
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