# Debugging

## VM Options
```
-Djava.library.path="path-to/java-freetype/cmake-build-debug/bin"
-ea
-Dcom.sun.management.jmxremote
-Xlog:gc*=debug:file=logs/gclog.txt 
-XX:MaxGCPauseMillis=8
```

## IntelliJ Settings

### Break on Exception Condition

Needed lines may vary depending on environment

```
!(this instanceof java.lang.ClassNotFoundException)
&& !(this instanceof java.lang.NumberFormatException)
&& !(this instanceof java.io.IOException)
&& !(this instanceof java.security.PrivilegedActionException)
&& !(this instanceof java.lang.NoSuchFieldError)
&& !(this instanceof java.lang.NoSuchFieldException)
&& !(this instanceof java.lang.NoSuchMethodError)
&& !(this instanceof java.lang.NoSuchMethodException)
&& !(this instanceof java.lang.UnsatisfiedLinkError)
&& !(this instanceof java.lang.IllegalAccessException)
&& !(this instanceof java.lang.NoClassDefFoundError)
&& !(this instanceof java.lang.IncompatibleClassChangeError)
&& !(this instanceof java.lang.InterruptedException)
&& !(this instanceof sun.nio.fs.WindowsException)
```

### Working directory
```out\bin```
