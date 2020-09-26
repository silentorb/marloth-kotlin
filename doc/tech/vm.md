# Experimental VM Settings

## Experimental Shenandoah GC
With default settings this seemes to be drastically increasing heap size.
That could probably be reduced with configuration, but also raises the question
whether (due to it's present experimental state) Shenandoah is only useful with excessive memory usage.
```
-XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC
```

## Experimental Graal JIT
Haven't noticed a performance difference yet when this is enabled
```
-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler
```
