package marloth.clienting.editing

import silentorb.mythic.editing.Editor
import silentorb.mythic.editing.FileItem
import silentorb.mythic.editing.FileItemType
import simulation.main.World

const val activeWorldKey = "active world"

fun updateEditorSyncing(world: World?, editor: Editor): Editor =
    if (world == null)
      editor
    else {
      val fileItem = activeWorldKey to FileItem(
          isVirtual = true,
          type = FileItemType.file,
          parent = null,
          name = activeWorldKey,
          fullPath = activeWorldKey,
      )
      val graphEntry = activeWorldKey to world.staticGraph.value
      editor.copy(
          fileItems = editor.fileItems + fileItem,
          graphLibrary = editor.graphLibrary + graphEntry,
      )
    }
