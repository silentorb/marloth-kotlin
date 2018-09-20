package rendering

import mythic.breeze.transformSkeleton
import mythic.glowing.DrawMethod
import mythic.glowing.checkError
import mythic.spatial.Pi
import mythic.spatial.getRotationMatrix
import org.joml.times
import rendering.meshes.Primitives
import scenery.Textures
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

fun simplePainter(elements: Primitives, element: MeshElement, effects: Shaders, textures: DynamicTextureLibrary) {
  for (e in elements) {
    val transform = if (e.transform != null)
      element.transform * e.transform
    else
      element.transform

    val orientationTransform = getRotationMatrix(transform)
    val material = e.material
    val texture = textures[material.texture]

    val config = ObjectShaderConfig(
        transform,
        color = material.color,
        glow = material.glow,
        normalTransform = orientationTransform,
        texture = texture
    )
    val effect = if (texture != null)
      effects.textured
    else
      effects.colored

    effect.activate(config)
    e.mesh.draw(DrawMethod.triangleFan)
  }
}

fun renderSimpleElement(gameRenderer: GameSceneRenderer, element: MeshElement) {
  val renderer = gameRenderer.renderer
  val mesh = renderer.meshes[element.mesh]!!
//  advancedPainter(mesh, renderer.renderer, element, renderer.effects)
//      humanPainter(renderer, mesh.primitives)(element, renderer.effects, childDetails)
//  } else {
    simplePainter(mesh.primitives, element, renderer.effects, renderer.renderer.textures)
//  }
}

fun renderElementGroup(gameRenderer: GameSceneRenderer, group: ElementGroup) {
  for (element in group.meshes) {
    renderSimpleElement(gameRenderer, element)
  }
}
