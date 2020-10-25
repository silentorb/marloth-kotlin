# Mythic Godot

An experimental library to build Godot as a DLL that can integrate with the Mythic game engine.

* The goal of this project is not to provide general Godot API bindings
* This project is intended to provide a C++ core that drives Godot, and a specialized JNI API for the Mythic JVM pipeline to call
* For example, instead of providing an entire Graphics API, the JVM code will pass marshalled scene data to the custom C++ code which will in turn use that data to update and render an active Godot scene

## Building

From the Godot source directory run something like:

```
scons -j6 custom_modules=../../games/marloth/projects/godot/native/modules platform=windows tools=no vsproj=yes target=debug 
```
module_arkit_enabled=no module_bmp_enabled=no module_enet_enabled=no module_gdnative_enabled=no module_gridmap_enabled=no module_jsonrpc_enabled=no module_mbedtls_enabled=no module_mobile_vr_enabled=no module_tga_enabled=no module_theora_enabled=no module_upnp_enabled=no module_webm_enabled=no module_webp_enabled=no module_webrtc_enabled=no module_websocket_enabled=no 

Debugging exports:
```
dumpbin /EXPORTS bin/godot.windows.opt.tools.64.dll
```
