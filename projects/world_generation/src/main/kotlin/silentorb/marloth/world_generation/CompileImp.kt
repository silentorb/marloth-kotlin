package silentorb.marloth.world_generation

import silentorb.imp.campaign.*
import silentorb.imp.core.PathKey
import silentorb.imp.core.defaultImpNamespace
import silentorb.imp.core.getGraphOutputNodes
import silentorb.imp.core.mergeNamespaces
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.library.standard.standardLibrary
import silentorb.mythic.fathom.fathomLibrary
import java.nio.file.Paths

fun newImpLibrary() =
    listOf(
        defaultImpNamespace(),
        standardLibrary(),
        fathomLibrary(),
    )

fun compileWorldGenerationCode(): GetSpatialNode {
  val workspaceUrl = Thread.currentThread().contextClassLoader.getResource("world/workspace.yaml")!!
  val (workspace, workspaceErrors) = loadWorkspace(Paths.get(workspaceUrl.toURI()).parent)
  val initialContext = newImpLibrary() + loadLibrariesFromJava(workspace)
  val (modules, moduleErrors) = loadAllModules(workspace, initialContext, codeFromFile)
  val context = getModulesExecutionArtifacts(initialContext, modules)
  val value = executeToSingleValue(context, PathKey("world", "output"))!!
  return value as? GetSpatialNode ?: throw Error("Invalid GetSpatialNode")
}
