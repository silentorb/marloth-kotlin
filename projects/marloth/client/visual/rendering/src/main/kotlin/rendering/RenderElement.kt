package rendering

import mythic.breeze.MultiAnimationPart
import mythic.breeze.transformAnimatedSkeleton
import mythic.glowing.DrawMethod
import mythic.spatial.*
import org.joml.times
import rendering.meshes.Primitive
import rendering.shading.ObjectShaderConfig
import rendering.shading.populateBoneBuffer
import scenery.MeshId

fun renderElement(renderer: SceneRenderer, primitive: Primitive, transform: Matrix, isAnimated: Boolean) {
  val orientationTransform = getRotationMatrix(transform)
  val material = primitive.material
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
  val effect = if (isAnimated && texture != null)
    renderer.effects.animated
  else if (isAnimated)
    renderer.effects.coloredAnimated
  else if (texture != null)
    renderer.effects.textured
  else
    renderer.effects.colored

  effect.activate(config)
  primitive.mesh.draw(DrawMethod.triangleFan)
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

private fun useMesh(meshes: ModelMeshMap, meshId: MeshId, action: (ModelMesh) -> Unit) {
  val mesh = meshes[meshId]
  if (mesh == null) {
    val debugMeshNotFound = 0
  } else {
    action(mesh)
  }
}

fun renderElementGroup(gameRenderer: GameSceneRenderer, group: ElementGroup) {
  val sceneRenderer = gameRenderer.renderer
  val armature = sceneRenderer.renderer.armatures[group.armature]
  val transforms = if (armature != null)
    armatureTransforms(armature, group)
  else
    null

  if (transforms != null) {
    populateBoneBuffer(sceneRenderer.renderer.boneBuffer, armature!!.transforms, transforms)
  }

  val meshes = sceneRenderer.renderer.meshes

  for (element in group.meshes) {
    useMesh(meshes, element.mesh) { mesh ->
      for (primitive in mesh.primitives) {
        val transform = getElementTransform(element, primitive, transforms)
        renderElement(gameRenderer.renderer, primitive, transform, armature != null)
      }
    }
  }

  if (armature != null) {
    for ((socketName, element) in group.attachments) {
      val bone = armature.sockets[socketName]
      if (bone == null) {
        val debugMissingBone = 0
      } else {
        useMesh(meshes, element.mesh) { mesh ->
          for (primitive in mesh.primitives) {
            val a: Vector3m = Vector3m()
            element.transform.getTranslation(a)
            val b: Vector3m= Vector3m()
            transforms!![bone].getTranslation(b)
            val transform = element.transform * transforms!![bone]// * Matrix().rotateX(-Pi / 2f)
            renderElement(gameRenderer.renderer, primitive, transform, false)
          }
        }
      }
    }
  }
}
