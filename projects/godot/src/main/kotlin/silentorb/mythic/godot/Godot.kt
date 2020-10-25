package silentorb.mythic.godot

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer

typealias NativePlatform = Pointer

interface GodotWrapper : Library {
  fun newWindowsPlatform(): NativePlatform
  fun deleteWindowsPlatform(platform: NativePlatform)
  fun startGodot(platform: NativePlatform, executable: String, argCount: Int, args: Array<String>): Int
  fun pumpEvents(platform: NativePlatform)
  fun updateGodotMain(): Boolean
  fun stopGodot(platform: NativePlatform): Int

  // Game Loop
  fun updateTiming()
  fun updatePhysicsStatic(): Boolean
  fun getTicks(): Long
  fun updateIdle(): Boolean
  fun updateDisplayStatic()
  fun postUpdateMiscStatic(idle_begin: Long): Boolean
}

fun newGodotWrapper(dir: String, libraryName: String): GodotWrapper {
  NativeLibrary.addSearchPath(libraryName, dir)
  return Native.load(
      libraryName,
      GodotWrapper::class.java
  ) as GodotWrapper
}
