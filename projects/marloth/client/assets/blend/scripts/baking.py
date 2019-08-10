import bpy


def create_and_select_texture_node(material):
    tree = material.node_tree


def bake_texture(original, material):
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

    create_and_select_texture_node(material)

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
