package rendering

import mythic.drawing.drawTextRaw
import mythic.drawing.getStaticCanvasDependencies
import mythic.drawing.prepareTextMatrix
import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.typography.*
import org.joml.Vector2i
import org.joml.Vector4i
import rendering.drawing.renderElementGroup
import rendering.shading.GeneralPerspectiveShader
import rendering.shading.ObjectShaderConfig
import rendering.shading.ShaderFeatureConfig
import rendering.shading.Shaders
import scenery.Camera


class SceneRenderer(
    val viewport: Vector4i,
    val renderer: Renderer,
    val camera: Camera,
    val cameraEffectsData: CameraEffectsData
) {

  val effects: Shaders
    get() = renderer.shaders

  val flat: GeneralPerspectiveShader
    get() = renderer.getShader(renderer.vertexSchemas.flat, ShaderFeatureConfig())

  fun drawLine(start: Vector3, end: Vector3, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
    renderer.dynamicMesh.load(listOf(start.x, start.y, start.z, end.x, end.y, end.z))

    flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.lines)
  }

  fun drawLines(values: List<Float>, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
    renderer.dynamicMesh.load(values)

    flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.lines)
  }

  fun drawPoint(position: Vector3, color: Vector4, size: Float = 1f) {
    globalState.pointSize = size
    renderer.dynamicMesh.load(listOf(position.x, position.y, position.z))
    flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.points)
  }

  fun drawSolidFace(vertices: List<Vector3>, color: Vector4) {
    renderer.dynamicMesh.load(vertices.flatMap { listOf(it.x, it.y, it.z) })

    flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.triangleFan)
  }

  fun drawOutlinedFace(vertices: List<Vector3>, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
    renderer.dynamicMesh.load(vertices.flatMap { listOf(it.x, it.y, it.z) })

    flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.lines)
  }

  fun drawText(content: String, position: Vector3, style: TextStyle) {
    val dimensions = Vector2i(viewport.z, viewport.w)
    val pos = rasterizeCoordinates(position, cameraEffectsData, dimensions)
    val config = TextConfiguration(content, pos, style)
    val textDimensions = calculateTextDimensions(config)
    val pos2 = Vector2(pos.x - textDimensions.x / 2f, pos.y)
    val pixelsToScalar = Matrix().scale(1f / dimensions.x, 1f / dimensions.y, 1f)
    val transform = prepareTextMatrix(pixelsToScalar, pos2)

    drawTextRaw(
        config,
        renderer.drawing.coloredImage,
        renderer.vertexSchemas.drawing.image,
        transform
    )
  }

  fun drawCircle(position: Vector3, radius: Float, method: DrawMethod) {
    val resources = getStaticCanvasDependencies()
    val mesh = resources.meshes.circle
    val transform = Matrix()
        .billboardSpherical(position, camera.position, Vector3(0f, 0f, 1f))
        .scale(radius)
    flat.activate(ObjectShaderConfig(
        transform = transform,
        color = Vector4(0.5f, 0.5f, 0f, 0.4f)
    ))

    mesh.draw(method)
  }

  fun drawText(content: String, position: Vector3, style: IndexedTextStyle) =
      drawText(content, position, resolveTextStyle(renderer.fonts, style))

  val meshes: ModelMeshMap
    get() = renderer.meshes
}

fun renderElements(sceneRenderer: SceneRenderer, opaqueElementGroups: ElementGroups, transparentElementGroups: ElementGroups) {
  for (group in opaqueElementGroups) {
    renderElementGroup(sceneRenderer, group)
  }

  globalState.depthWrite = false
  for (group in transparentElementGroups) {
    renderElementGroup(sceneRenderer, group)
  }
  globalState.depthWrite = true
}
