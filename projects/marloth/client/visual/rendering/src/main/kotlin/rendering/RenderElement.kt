package rendering

import mythic.breeze.MultiAnimationPart
import mythic.breeze.transformAnimatedSkeleton
import mythic.glowing.DrawMethod
import mythic.spatial.getRotationMatrix
import org.joml.times
import rendering.meshes.Primitive

fun simplePainter(renderer: SceneRenderer, primitive: Primitive, element: MeshElement, isAnimated: Boolean) {
  val transform = if (primitive.transform != null)
    element.transform * primitive.transform
  else
    element.transform

  val orientationTransform = getRotationMatrix(transform)
  val material = primitive.material
  val texture = renderer.renderer.textures[material.texture]

  if (material.texture != null && texture == null) {
    val k = 0
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

fun renderElementGroup(gameRenderer: GameSceneRenderer, group: ElementGroup) {
  val sceneRenderer = gameRenderer.renderer
  val armature = sceneRenderer.renderer.armatures[group.armature]
  val transforms = if (armature != null) {
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
  } else
    null

  if (transforms != null) {
    populateBoneBuffer(sceneRenderer.renderer.boneBuffer, armature!!.transforms, transforms)
  }

  val meshes = sceneRenderer.renderer.meshes
  for (element in group.meshes) {
    val mesh = meshes[element.mesh]!!
    for (primitive in mesh.primitives) {
      simplePainter(gameRenderer.renderer, primitive, element, armature != null)
    }
  }
}
