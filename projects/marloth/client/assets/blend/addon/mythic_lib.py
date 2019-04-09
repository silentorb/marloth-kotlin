import bpy

def bake_texture(original):
    print('Baking texture for ' + original.name)
    bpy.context.scene.objects.active = original
    original.select = True
    original.hide = False

    # Prepare layers to hide all objects that might interfere with the baking
    def set_layer(obj, layer):
        obj.layers = [i == layer for i in range(len(obj.layers))]

    # Create proxy
    bpy.ops.object.duplicate()
    proxy = bpy.context.active_object

    # Prepare rendering
    proxy_modifiers = []
    for mod in proxy.modifiers:
        proxy_modifiers.append(mod)

    for mod in proxy_modifiers:
        bpy.ops.object.modifier_apply(modifier=mod.name)

    proxy.hide_render = False
    original.hide_render = True

    original.select = True
    bpy.context.scene.objects.active = original

    set_layer(original, 1)
    set_layer(proxy, 1)
    bpy.context.scene.layers[1] = True
    bpy.context.scene.layers[0] = False

    material = bpy.data.materials[original.name]
    select_material_texture_node(material)

    # Bake
    image_name = original.name
    image = bpy.data.images[image_name]
    bpy.ops.object.bake()
    image.save()

    # Cleanup
    bpy.data.objects.remove(proxy)

    set_layer(original, 0)
    bpy.context.scene.layers[0] = True
    bpy.context.scene.layers[1] = False
    original.hide_render = False


def bake_selected():
    bake_texture(bpy.context.active_object)


def initialize_texture(image_name):
    image = bpy.data.images.new(image_name, 512, 512)
    image.filepath_raw = '//images/' + image_name + '.jpg'
    image.file_format = 'JPEG'
    image.save()


def initialize_object_texture(obj):
    initialize_texture(obj.name)


def get_material_texture_node(material):
    tree = material.node_tree
    for node in tree.nodes:
        if node.bl_idname == 'ShaderNodeTexImage' and node.image.name == material.name:
            return node
    return None


def select_material_texture_node(material):
    node = get_material_texture_node(material)
    node.select = True
    material.node_tree.nodes.active = node


def is_bake_material(material):
    if not material.use_nodes:
        return False

    return get_material_texture_node(material) != None


def get_bake_materials():
    result = []
    for material in bpy.data.materials:
        if is_bake_material(material):
            result.append(material)
    return result


def bake_all_textures():
    materials = get_bake_materials()
    for material in materials:
        obj = bpy.data.objects[material.name]
        bake_texture(obj)


class MythicBakeObjectTextureOperator(bpy.types.Operator):
    bl_idname = "mythic.bake_object_texture"
    bl_label = "Bake Object Texture"

    def execute(self, context):
        bake_texture(bpy.context.active_object)
        return {'FINISHED'}


class MythicBakeAllTexturesOperator(bpy.types.Operator):
    bl_idname = "mythic.bake_all_textures"
    bl_label = "Bake All Textures"

    def execute(self, context):
        bake_all_textures()
        return {'FINISHED'}


class MythicInitializeObjectTextureOperator(bpy.types.Operator):
    bl_idname = "mythic.initialize_object_texture"
    bl_label = "Inititalize Object Texture"

    def execute(self, context):
        initialize_object_texture(bpy.context.active_object)
        return {'FINISHED'}


class MainMythicMenu(bpy.types.Menu):
    bl_label = "Mythic"
    bl_idname = "mythic_menu_main"

    def draw(self, context):
        layout = self.layout

        layout.operator("mythic.bake_object_texture")
        layout.operator("mythic.bake_all_textures")
        layout.operator("mythic.initialize_object_texture")


def append_mythic_menu(self, context):
    print('appending')
    layout = self.layout
    layout.menu("mythic_menu_main")


def append_mythic_menu(self, context):
    print('appending')
    layout = self.layout
    layout.menu("mythic_menu_main")


components = [
    MythicBakeObjectTextureOperator,
    MythicBakeAllTexturesOperator,
    MythicInitializeObjectTextureOperator,
    MainMythicMenu
]


original_main_menu_function = None


def main_menu_wrapper(layout, context):
    print('appending')
    original_main_menu_function(layout, context)
    layout.menu("mythic_menu_main")


def register():
    for component in components:
        bpy.utils.register_class(component)
    print('registering mythic')
    global original_main_menu_function
    original_main_menu_function = bpy.types.INFO_MT_editor_menus.draw_menus
    bpy.types.INFO_MT_editor_menus.draw_menus = main_menu_wrapper


def unregister():
    for component in components:
        bpy.utils.unregister_class(component)

    # bpy.types.IMAGE_MT_image.remove(append_mythic_menu)
    bpy.types.INFO_MT_editor_menus.draw_menus = original_main_menu_function


if __name__ == "__main__":
    register()
