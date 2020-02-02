
//val rayMarchOperator: TextureFunction =
//    { dimensions ->
//      {arguments ->
//        val marcher = Marcher(
//            end = 50f,
//            maxSteps = 100
//        )
//
//        val scene = Scene(
//            camera = Camera(
//                position = Vector3(-5f, 0f, 0f),
//                orientation = Quaternion(),
//                near = 0.01f,
//                far = 100f
//            ),
//            sdf = { sampleScene2() },
//            lights = listOf()
//        )
//
//        val buffers = newMarchBuffers(dimensions.x * dimensions.y)
//
////        val cast = orthogonalRay(scene.camera, 1f)
//        val cast = perspectiveRay(scene.camera)
//        renderToMarchBuffers(buffers, marcher, scene, cast, dimensions)
//
//        mapOf(
//            "color" to buffers.color,
//            "depth" to buffers.depth,
//            "position" to buffers.position,
//            "normal" to buffers.normal
//        )
//      }
//    }

//val illuminationOperator: TextureFunction = withBuffer(withGrayscaleBuffer) {arguments ->
//  val depth = floatBufferArgument(arguments, "depth")
//  val position = floatBufferArgument(arguments, "position")
//  val normal = floatBufferArgument(arguments, "normal")
//  val lights = listOf(
//      Light(position = Vector3(3f, -3f, 3f), brightness = 1f),
//      Light(position = Vector3(-3f, 0f, 2f), brightness = 0.8f)
//  )
//  val k = 0
//  { x, y ->
//    illuminatePoint(lights, depth.get(), position.getVector3(), normal.getVector3())
//  }
//}
//
//val mixSceneOperator: TextureFunction =
//    { dimensions ->
//      {arguments ->
//        val color = floatBufferArgument(arguments, "color")
//        val illumination = floatBufferArgument(arguments, "illumination")
//        val k = 0
//        fillBuffer(id, 3, dimensions) { buffer ->
//          for (y in 0 until dimensions.y) {
//            for (x in 0 until dimensions.x) {
//              buffer.put(mixColorAndLuminance(color.getVector3(), illumination.get()))
//            }
//          }
//        }
//      }
//    }
//
//val toneMapping: TextureFunction =
//    { length ->
//      {arguments ->
//        val input = arguments["input"]!! as FloatBuffer
//        compressRange(input)
//      }
//    }
