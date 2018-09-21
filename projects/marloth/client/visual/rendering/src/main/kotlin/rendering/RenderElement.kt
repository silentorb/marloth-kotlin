package rendering

import mythic.breeze.transformAnimatedSkeleton
import mythic.glowing.DrawMethod
import mythic.spatial.getRotationMatrix
import org.joml.times
import rendering.meshes.Primitive
import rendering.meshes.Primitives

/*
fun advancedPainter(mesh: AdvancedModel, renderer: Renderer, element: MeshElement, effects: Shaders) {
  val transform = element.transform.rotateZ(Pi / 2).scale(2f)
  val orientationTransform = getRotationMatrix(transform)
  for (e in mesh.primitives) {
    val material = e.material
    val animation = element.animations
    val boneBuffer = if (animation != null) {
      val transforms = transformSkeleton(animation.armature.bones)
//      val transforms = animation.armature.bones.mapIndexed { i, bone ->

//        Matrix(transforms[i]) * Matrix(originalTransform).invert()
//      }
      populateBoneBuffer(renderer.boneBuffer, animation.armature.transforms, transforms)
    } else
      null

    val shaderConfig = ObjectShaderConfig(
        transform = transform,
        glow = material.glow,
        normalTransform = orientationTransform,
        color = material.color,
        texture = renderer.mappedTextures[Textures.checkers]!!,
        boneBuffer = boneBuffer
    )
    effects.textured.activate(shaderConfig)
//        effects.flatAnimated.activate(shaderConfig)
    checkError("drawing animated mesh-pre")
    e.mesh.draw(DrawMethod.triangleFan)
    checkError("drawing animated mesh")
  }
}
*/

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
  val effect = if (isAnimated)
    renderer.effects.coloredAnimated
  else if (texture != null)
    renderer.effects.textured
  else
    renderer.effects.colored

  effect.activate(config)
  primitive.mesh.draw(DrawMethod.triangleFan)
}

//fun renderSimpleElement(gameRenderer: GameSceneRenderer, element: MeshElement) {
//  val renderer = gameRenderer.renderer
//  val mesh = renderer.meshes[element.mesh]!!
////  advancedPainter(mesh, renderer.renderer, element, renderer.effects)
////      humanPainter(renderer, mesh.primitives)(element, renderer.effects, childDetails)
////  } else {
//  simplePainter(renderer, mesh, element)
////  }
//}

fun renderElementGroup(gameRenderer: GameSceneRenderer, group: ElementGroup) {
  val sceneRenderer = gameRenderer.renderer
  val armature = sceneRenderer.renderer.armatures[group.armature]
  val transforms = if (armature != null) {
    val animation = group.animations.first()
    transformAnimatedSkeleton(armature, armature.animations[animation.animation], animation.timeOffset)
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
