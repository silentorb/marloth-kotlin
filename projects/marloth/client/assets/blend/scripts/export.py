import bpy
import os
import os.path
import sys

sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'addon'))
from mythic_lib import get_material_texture_node

models_path = 'src/main/resources/models'


def add_to_map_list(map, key, item):
    if key in map:
        map[key].append(item)
    else:
        map[key] = [item]


def of_type(list, type):
    return [c for c in list if c.type == type]


def get_horizontal_radius(dimensions):
    return max(dimensions.x, dimensions.y) / 2


def preprocess_bounds_shape(obj):
    bounds_type_key = 'bounds'
    type = obj.get(bounds_type_key)
    if type is None:
        return None

    del obj[bounds_type_key]

    dimensions = obj.dimensions
    if type == 'cylinder':
        obj['bounds'] = {
            'type': 'cylinder',
            'radius': get_horizontal_radius(dimensions),
            'height': dimensions.z
        }


def has_dominant_material(obj):
    return any(m.name == obj.name for m in obj.material_slots)


def remove_materials(object, material_slot_indices):
    for i in reversed(material_slot_indices):
        object.active_material_index = i
        bpy.ops.object.material_slot_remove()


# Used for objects that have a primary material for baking and secondary materials to assist in baking
def prune_materials(object):
    not_needed = []
    if has_dominant_material(object):
        not_needed = [i for i, m in enumerate(object.material_slots) if m.name != object.name]
    # Eventually it would be nice to filter out reference materials
    # but currently blendergltf isn't exporting UV maps without them (even though UV maps are independent of materials)
    # TODO: Enable exporting UV Maps without materials.
    # else:
    #     not_needed = [i for i, m in enumerate(object.material_slots) if m.name == 'reference']

    remove_materials(object, not_needed)


# Blender Cycles is duct-taped ontop of the older Blender Renderer.  One side effect of this duct-taping is
# Blender Cycles does not need Texture objects and can use Texture nodes instead, but the blendergltf exporter
# only looks for Texture objects.
# Blender Texture objects are not the same thing as Blender Image objects.
# (Mythic's python code does not make such a distinction.)
# Depending on silly hidden nuances in Blender's GUI, using Cycles may or may not result in texture objects getting
# created and assigned to a materials.  (Cycles works the same whether that happens or not.)
# This function ensures all materials using texture nodes also have texture objects.
def create_missing_image_textures():
    for material in bpy.data.materials:
        texture_node = get_material_texture_node(material)
        if texture_node:
            texture_name = material.name
            current_texture = bpy.data.textures.get(texture_name)
            if not current_texture:
                image = bpy.data.images[texture_name]
                if image:
                    print('creating texture record for ' + texture_name)
                    texture = bpy.data.textures.new(texture_name, 'IMAGE')
                    texture.image = image
                    material.active_texture = texture


def gather_ik_bones(pose):
    constraints = {}
    for bone in pose.bones:
        for constraint in of_type(bone.constraints, 'IK'):
            key = str(constraint.subtarget)
            add_to_map_list(constraints, key, bone.name)
            b = bone
            while b.parent and b.bone.use_connect:
                b = b.parent
                add_to_map_list(constraints, key, b.name)
    return constraints


def add_keyframe_if_missing(curves, obj_name, property_name, values):
    data_path = 'pose.bones["' + obj_name + '"].' + property_name
    for curve in curves:
        if curve.data_path == data_path:
            return

    for i, value in enumerate(values):
        curve = curves.new(data_path, i, obj_name)
        curve.keyframe_points.insert(0, value)


def prepare_armature(armature):
    bones = []
    if 'rig' in bpy.data.objects.keys():
        rig = bpy.data.objects['rig']
        bones = [bone.name for bone in rig.data.bones if bone.use_deform]

    for action in bpy.data.actions:
        for bone_name in bones:
            if next((g for g in action.groups if g.name == bone_name), None) is None:
                action.groups.new(bone_name)

            add_keyframe_if_missing(action.fcurves, bone_name, 'location', [0, 0, 0])
            add_keyframe_if_missing(action.fcurves, bone_name, 'rotation_quaternion', [1, 0, 0, 0])


# This little hack allows Blender files to treat the render visibility flag as a
# flag for whether a node should be exported.
# This way, objects can be visible in the blender file but and not exported, (Such as helper objects)
# while other objects can be hidden in the blender file but exported, such as objects that are not
# currently being worked on.  This comes into play a lot when a blender file has many different versions
# of an asset that should only be used one at a time, such as multiple outfits.  When working with such
# a file usually only one outfit is visible at a time.
def hide_unrenderable():
    for obj in bpy.context.scene.objects:
        obj.hide = obj.hide_render


def prepare_scene():
    for armature in of_type(bpy.data.objects, 'ARMATURE'):
        prepare_armature(armature)

    hide_unrenderable()

    for obj in bpy.context.scene.objects:
        if not obj.hide:
            prune_materials(obj)

    for obj in bpy.context.scene.objects:
        preprocess_bounds_shape(obj)

    create_missing_image_textures()


def get_blend_filename():
    filepath = bpy.data.filepath
    return os.path.splitext(os.path.basename(filepath))[0]


def get_export_filepath():
    name = get_blend_filename()
    script_path = os.path.realpath(__file__)
    return os.path.abspath(os.path.join(os.path.dirname(script_path), '../../', models_path, name, name + '.gltf'))


def export_gltf():
    filepath = get_export_filepath()
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    bpy.ops.export_scene.gltf(
        filepath=filepath,
        axis_forward='Y',
        axis_up='Z',
        draft_prop=False,
        nodes_export_hidden=False,
        nodes_selected_only=False,
        materials_disable=False,
        meshes_apply_modifiers=True,
        meshes_vertex_color_alpha=False,
        meshes_interleave_vertex_data=True,
        # Make sure all of these are ELIGIBLE and not ACTIVE or duplicate, or malformed animations will get exported
        # and overwrite the proper animations because they end up sharing the same names in mythic.
        animations_object_export='ELIGIBLE',
        animations_armature_export='ELIGIBLE',
        animations_shape_key_export='ELIGIBLE',
        images_data_storage='COPY',
        images_allow_srgb=False,
        buffers_embed_data=False,
        buffers_combine_data=True,
        asset_copyright='',
        asset_version='2.0',
        asset_profile='WEB',
        gltf_export_binary=False,
        pretty_print=True,
        blocks_prune_unused=True,
        enable_actions=True,
        enable_cameras=False,
        enable_lamps=False,
        enable_materials=True,
        enable_meshes=True,
        enable_textures=True
    )


if __name__ == '__main__':
    prepare_scene()
    export_gltf()
    print('Exported ', get_export_filepath())
