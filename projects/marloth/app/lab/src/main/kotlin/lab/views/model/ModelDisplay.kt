package lab.views.model

import lab.utility.*
import lab.views.game.renderFaceNormals
import mythic.bloom.*
import mythic.breeze.MultiAnimationPart
import mythic.breeze.transformAnimatedSkeleton
import mythic.drawing.Canvas
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.spatial.*
import org.joml.*
import rendering.*
import rendering.meshes.Primitive
import rendering.meshes.modelToMeshes
import rendering.shading.ObjectShaderConfig
import rendering.shading.populateBoneBuffer
import scenery.AnimationId
import scenery.Camera
import scenery.ProjectionType
import scenery.Scene

fun createOrthographicCamera(camera: ViewCameraConfig): Camera {
  val orientation = Quaternion()
      .rotateZ(camera.rotationZ)
      .rotateY(camera.rotationY)

  val position = orientation * Vector3(12f, 0f, 0f)
  val orientationSecond = Quaternion().rotateTo(Vector3(1f, 0f, 0f), -position)
  return Camera(ProjectionType.orthographic, position + camera.pivot, orientationSecond, camera.zoom)
}

fun drawMeshPreview(config: ModelViewConfig, sceneRenderer: SceneRenderer, transform: Matrix, section: Primitive, isAnimated: Boolean) {
  val mesh = section.mesh

  globalState.depthEnabled = true
  globalState.blendEnabled = true
  globalState.cullFaces = true

//  if (bones != null && originalBones != null) {
//    val shaderConfig = ObjectShaderConfig(
//        transform = transform,
//        color = section.material.color,
//        boneBuffer = populateBoneBuffer(sceneRenderer.renderer.boneBuffer, bones)
//    )
//    when (config.meshDisplay) {
//      MeshDisplay.solid -> sceneRenderer.effects.flatAnimated.activate(shaderConfig)
//      MeshDisplay.wireframe -> sceneRenderer.effects.flatAnimated.activate(shaderConfig)
//    }
//  } else {
  val color = if (config.meshDisplay == MeshDisplay.solid)
    section.material.color
  else
    faceColor

  val texture = sceneRenderer.renderer.textures[section.material.texture]
  val shaderConfig = ObjectShaderConfig(
      transform = transform,
      color = color,
      texture = texture,
      normalTransform = Matrix()
  )
  val effects = sceneRenderer.effects
  val (effect, flatEffect) = if (isAnimated)
    Pair(effects.flatAnimated, effects.flatAnimated)
  else if (texture != null)
    Pair(effects.texturedFlat, effects.flat)
  else
    Pair(effects.flat, effects.flat)

  effect.activate(shaderConfig)

  mesh.draw(DrawMethod.triangleFan)
  if (config.meshDisplay == MeshDisplay.wireframe) {
    globalState.cullFaces = false
    globalState.depthEnabled = false
  }

  val shaderConfig2 = ObjectShaderConfig(
      transform = transform,
      color = lineColor
  )

  globalState.lineThickness = 1f
  flatEffect.activate(shaderConfig2)
  mesh.draw(DrawMethod.lineLoop)

  globalState.pointSize = 3f
  flatEffect.activate(shaderConfig2)
  mesh.draw(DrawMethod.points)

  globalState.depthEnabled = false
  globalState.cullFaces = false
}

fun drawSelection(config: ModelViewConfig, model: Model, sceneRenderer: SceneRenderer) {
  if (config.selection.size > 0) {
    when (config.componentMode) {

      ComponentMode.faces -> {
        val faces = model.mesh.faces.values.toList()
        for (index in config.selection) {
          if (faces.size > index) {
            val face = faces[index]
            sceneRenderer.drawSolidFace(face.vertices, white)
          }
        }
      }

      ComponentMode.edges -> {
        val edges = model.edges
        for (index in config.selection) {
          if (edges.size > index) {
            val edge = edges[index]
            sceneRenderer.drawLine(edge.first, edge.second, white, 2f)
          }
        }
      }

      ComponentMode.vertices -> {
        val vertices = model.vertices
        for (index in config.selection) {
          if (vertices.size > index)
            sceneRenderer.drawPoint(vertices[index], white, 2f)
        }
      }

    }
  }
}

fun drawModelPreview(config: ModelViewConfig, state: ModelViewState, renderer: Renderer, camera: Camera, model: AdvancedModel): Depiction {
  return embedCameraView { b, c ->
    val sceneRenderer = renderer.createSceneRenderer(Scene(camera), b.toVector4i())
    val transform = Matrix()

    val armature = model.armature
    val modelSource = model.model

//    val bones = if (armature != null) getAnimatedBones(armature, state.animationOffset) else null
    val transforms = if (armature != null) {
//      transformAnimatedSkeleton(armature.bones, armature.animations[state.animation]!!, state.animationElapsedTime)
      val a = listOf(
          MultiAnimationPart(
              animation = armature.animations[AnimationId.stand]!!,
              timeOffset = state.animationElapsedTime,
              strength = 0f
          ),
          MultiAnimationPart(
              animation = armature.animations[AnimationId.walk]!!,
              timeOffset = state.animationElapsedTime,
              strength = 1f
          )
      )
      transformAnimatedSkeleton(armature.bones, a)
    } else
      null

    if (transforms != null) {
      populateBoneBuffer(sceneRenderer.renderer.boneBuffer, armature!!.transforms, transforms)
    }

    if (modelSource == null) {
      model.primitives
          .filterIndexed { i, it -> config.visibleGroups[i] }
          .forEach { drawMeshPreview(config, sceneRenderer, transform, it, transforms != null) }
    } else {
      val primitives2 = model.primitives

      val meshes = modelToMeshes(renderer.vertexSchemas, modelSource)
      meshes
          .filterIndexed { i, it -> config.visibleGroups[i] }
          .forEach {
            drawMeshPreview(config, sceneRenderer, transform, it, transforms != null)
          }

      meshes.forEach { it.mesh.dispose() }

//      if (config.drawNormals)
//        renderFaceNormals(sceneRenderer, 0.05f, modelSource.mesh)

//      modelSource.mesh.edges.values.filter { it.faces.none() }.forEach {
//        sceneRenderer.drawLine(it.first, it.second, Vector4(0.8f, 0.5f, 0.3f, 1f))
//      }

      if (config.drawTempLine)
        sceneRenderer.drawLine(config.tempStart, config.tempEnd, yellow)

      drawSelection(config, modelSource, sceneRenderer)

      globalState.depthEnabled = false

      for (group in modelSource.info.edgeGroups) {
        for (pair in group) {
          sceneRenderer.drawLine(Vector3(pair.key.first), Vector3(pair.key.second), yellow)
        }
      }
    }

    globalState.depthEnabled = true

    sceneRenderer.drawLine(Vector3(), Vector3(1f, 0f, 0f), red)
    sceneRenderer.drawLine(Vector3(), Vector3(0f, 1f, 0f), green)
    sceneRenderer.drawLine(Vector3(), Vector3(0f, 0f, 1f), blue)

    if (armature != null) {
      globalState.depthEnabled = false
      drawSkeleton(sceneRenderer, armature, transforms!!, Matrix())
      globalState.depthEnabled = true
    }
  }
}

fun drawBackground(backgroundColor: Vector4): Depiction = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}


