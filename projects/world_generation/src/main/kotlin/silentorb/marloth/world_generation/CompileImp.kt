package silentorb.marloth.world_generation

import silentorb.imp.campaign.codeFromFile
import silentorb.imp.campaign.getModulesExecutionArtifacts
import silentorb.imp.campaign.loadAllModules
import silentorb.imp.campaign.loadWorkspace
import silentorb.imp.core.PathKey
import silentorb.imp.core.defaultImpNamespace
import silentorb.imp.core.getGraphOutputNodes
import silentorb.imp.core.mergeNamespaces
import silentorb.imp.execution.executeToSingleValue
import silentorb.imp.library.standard.standardLibrary
import java.nio.file.Paths

fun newImpLibrary() =
    listOf(
        defaultImpNamespace(),
        standardLibrary(),
    )

fun compileWorldGenerationCode(): GetSpatialNode {
  val initialContext = newImpLibrary()
  val workspaceUrl = Thread.currentThread().contextClassLoader.getResource("world/workspace.yaml")!!
  val (workspace, errors) = loadWorkspace(Paths.get(workspaceUrl.toURI()).parent)
  val (modules) = loadAllModules(workspace, initialContext, codeFromFile)
  val context = getModulesExecutionArtifacts(initialContext, modules)
  val outputs = getGraphOutputNodes(mergeNamespaces(context))
  val value = executeToSingleValue(context, PathKey("world", "output"))!!
  return value as? GetSpatialNode ?: throw Error("Invalid GetSpatialNode")
}
