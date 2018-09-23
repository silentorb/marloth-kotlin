package rendering

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
    val animation = group.animations.first()
    transformAnimatedSkeleton(armature.bones, armature.animations[animation.animationId]!!, animation.timeOffset)
  } else
    null

  if (transforms != null) {
    populateBoneBuffer(sceneRenderer.renderer.boneBuffer, armature!!.transforms, transforms)
  }
  for (element in group.meshes) {
    val primitives = sceneRenderer.renderer.meshMap[element.mesh]!!
    for (primitive in primitives) {
      simplePainter(gameRenderer.renderer, primitive, element, armature != null)
    }
  }
}