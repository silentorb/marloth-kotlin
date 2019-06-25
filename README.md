# Marloth Kotlin

This project depends on the following external media software:
* Blender 2.79
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

### Working directory
```out\bin```
