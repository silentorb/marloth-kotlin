package silentorb.mythic.godot

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary

interface GodotWrapper : Library {
    fun mythicMain(executable: String, argCount: Int, args: Array<String>): Int
}

fun newGodotWrapper(dir: String, libraryName: String): GodotWrapper {
    NativeLibrary.addSearchPath(libraryName, dir)
    return Native.load(
        libraryName,
        GodotWrapper::class.java
    ) as GodotWrapper
}
