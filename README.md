# Marloth Kotlin

## Setup

This project depends on the following external media software:
* Blender 2.81
    * Currently depends on version 1.0.10 of the gltf exporter (for animation extras to serialize markers), which isn't yet shipped with Blender and needs to be manually copied over the bundled version
* Filter Forge 8

## IntelliJ Settings

### Java Debugging
```
!(this instanceof java.lang.ClassNotFoundException)
&& !(this instanceof java.lang.NumberFormatException)
&& !(this instanceof java.io.IOException)
&& !(this instanceof java.security.PrivilegedActionException)
&& !(this instanceof java.lang.NoSuchFieldError)
&& !(this instanceof java.lang.NoSuchFieldException)
&& !(this instanceof java.lang.NoSuchMethodError)
&& !(this instanceof java.lang.NoSuchMethodException)
&& (!(this instanceof java.lang.NullPointerException) || (this instanceof kotlin.KotlinNullPointerException))
&& !(this instanceof java.lang.UnsatisfiedLinkError)
&& !(this instanceof java.lang.IllegalAccessException)
&& !(this instanceof java.lang.NoClassDefFoundError)
&& !(this instanceof java.lang.IncompatibleClassChangeError)
&& !(this instanceof java.lang.InterruptedException)
&& !(this instanceof sun.nio.fs.WindowsException)
```

```
&& !(this instanceof kotlin.reflect.jvm.internal.KotlinReflectionInternalError)
```

```
!(this instanceof java.lang.ClassNotFoundException)
&& !(this instanceof java.io.IOException)
&& !(this instanceof java.security.PrivilegedActionException)
&& !(this instanceof kotlin.reflect.jvm.internal.KotlinReflectionInternalError)
&& !(this instanceof sun.nio.fs.WindowsException)
```

```
&& (this.getClass().getName() != "kotlin.reflect.jvm.internal.KotlinReflectionInternalError")
```
### VM Options
```
-Djava.library.path="E:/dev/games/java-freetype/cmake-build-debug/bin" -ea -Dcom.sun.management.jmxremote
-Xlog:gc*=debug:file=logs/gclog.txt 
-XX:MaxGCPauseMillis=8
```

### Experimental Shenandoah GC
With default settings this seemes to bedrastically increasing heap size.
That could probably be reduced with configuration, but also raises the question
whether (due to it's present experimental state) Shenandoah is only useful with excessive memory usage.
```
-XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC
```

### Experimental Graal JIT
Haven't noticed a performance difference yet when this is enabled
```
-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler
```
### Working directory
```out\bin```
