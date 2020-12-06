package silentorb.marloth.world_generation.imp

import silentorb.imp.core.Namespace
import silentorb.imp.execution.newLibrary

fun newWorldGenerationLibrary(): Namespace =
    newLibrary(
        functions = worldGenerationFunctions()
    )
