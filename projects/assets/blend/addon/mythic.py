import importlib.util

bl_info = {
    "name": "Mythic Tools",
    "category": "Development",
    "author": "Christopher W. Johnson"
}

addon_path = 'E:/dev/games/dev_lab/projects/marloth/client/assets/blend/addon'
spec = importlib.util.spec_from_file_location("mythic", addon_path + "/mythic_lib.py")
mythic_lib = importlib.util.module_from_spec(spec)
spec.loader.exec_module(mythic_lib)


def register():
    mythic_lib.register()


def unregister():
    mythic_lib.unregister()
