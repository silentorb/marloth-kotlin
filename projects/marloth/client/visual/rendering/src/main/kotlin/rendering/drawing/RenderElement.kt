package rendering.drawing

import mythic.breeze.MultiAnimationPart
import mythic.breeze.transformAnimatedSkeleton
import mythic.glowing.DrawMethod
import mythic.glowing.drawMesh
import mythic.spatial.*
import org.joml.times
import rendering.*
import rendering.meshes.Primitive
import rendering.shading.ObjectShaderConfig
import rendering.shading.ShaderFeatureConfig
import rendering.shading.populateBoneBuffer
import scenery.MeshName

fun renderElement(renderer: SceneRenderer, primitive: Primitive, material: Material, transform: Matrix, isAnimated: Boolean) {
  val orientationTransform = getRotationMatrix(transform)
  val texture = renderer.renderer.textures[material.texture]

  if (material.texture != null && texture == null) {
    val debugMissingTexture = 0
  }
  val config = ObjectShaderConfig(
      transform,
      color = material.color,
      glow = material.glow,
      normalTransform = orientationTransform,
      texture = texture
  )
  val effect = renderer.renderer.getShader(primitive.mesh.vertexSchema, ShaderFeatureConfig(
      skeleton = isAnimated,
      texture = texture != null
  ))

  effect.activate(config)
  drawMesh(primitive.mesh, DrawMethod.triangleFan)
}

fun armatureTransforms(armature: Armature, group: ElementGroup): List<Matrix> =
    if (group.animations.size == 1) {
      val animation = group.animations.first()
      transformAnimatedSkeleton(armature.bones, armature.animations[animation.animationId]!!, animation.timeOffset)
    } else {
      val animations = group.animations.map { animation ->
        MultiAnimationPart(
            animation = armature.animations[animation.animationId]!!,
            strength = animation.strength,
            timeOffset = animation.timeOffset
        )
      }
      transformAnimatedSkeleton(armature.bones, animations)
    }

fun getElementTransform(element: MeshElement, primitive: Primitive, transforms: List<Matrix>?): Matrix {
  return if (primitive.transform != null)
    element.transform * primitive.transform
  else if (primitive.parentBone != null && transforms != null)
    element.transform * transforms[primitive.parentBone] * Matrix().rotateX(-Pi / 2f)
  else
    element.transform
}

private fun useMesh(meshes: ModelMeshMap, MeshName: MeshName, action: (ModelMesh) -> Unit) {
  val mesh = meshes[MeshName]
  if (mesh == null) {
    val debugMeshNotFound = 0
  } else {
    action(mesh)
  }
}

fun renderMeshElement(sceneRenderer: SceneRenderer, element: MeshElement, armature: Armature? = null, transforms: List<Matrix>? = null) {
  val meshes = sceneRenderer.renderer.meshes
  useMesh(meshes, element.mesh) { mesh ->
    for (primitive in mesh.primitives) {
      val transform = getElementTransform(element, primitive, transforms)
      val materal = element.material ?: primitive.material
      val isAnimated = armature != null && primitive.isAnimated
      renderElement(sceneRenderer, primitive, materal, transform, isAnimated)
    }
  }
}

fun renderElementGroup(sceneRenderer: SceneRenderer, group: ElementGroup) {
  val armature = sceneRenderer.renderer.armatures[group.armature]
  val transforms = if (armature != null)
    armatureTransforms(armature, group)
  else
    null

  if (transforms != null) {
    populateBoneBuffer(sceneRenderer.renderer.uniformBuffers.bone, armature!!.transforms, transforms)
  }

  for (element in group.meshes) {
    renderMeshElement(sceneRenderer, element, armature, transforms)
  }

  if (armature != null) {
    for ((socketName, element) in group.attachments) {
      val bone = armature.sockets[socketName]
      if (bone == null) {
        val debugMissingBone = 0
      } else {
        val meshes = sceneRenderer.renderer.meshes
        useMesh(meshes, element.mesh) { mesh ->
          for (primitive in mesh.primitives) {
            val transform = element.transform * transforms!![bone]
            val materal = element.material ?: primitive.material
            renderElement(sceneRenderer, primitive, materal, transform, false)
          }
        }
      }
    }
  }

  if (group.billboards.any()) {
    renderBillboard(sceneRenderer, group.billboards)
  }
}
