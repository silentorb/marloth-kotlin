import bpy
import os
import os.path
import json

models_path = 'src/main/resources/models'

def add_to_map_list(map, key, item):
    if key in map:
        map[key].append(item)
    else:
        map[key] = [item]

def of_type(list, type):
    return [c for c in list if c.type == type]

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
        # print('adding curve ' + data_path)
        curve = curves.new(data_path, i, obj_name)
        curve.keyframe_points.insert(0, value)

def prepare_armature(armature):
    constraints = gather_ik_bones(armature.pose)
    # print(json.dumps(constraints, indent=4))
    bones = []
    if 'rig' in bpy.data.objects.keys():
        rig = bpy.data.objects['rig']
        bones = [bone.name for bone in rig.data.bones if bone.use_deform]

    for action in bpy.data.actions:
        # print("Action: " + action.name)

        # for k, ik in constraints.items():
        #     for bone_name in ik:
        for bone_name in bones:
            # print("adding keyframes " + bone_name)
            if next((g for g in action.groups if g.name == bone_name), None) == None:
                action.groups.new(bone_name)

            add_keyframe_if_missing(action.fcurves, bone_name, 'location',[0,0,0])
            add_keyframe_if_missing(action.fcurves, bone_name, 'rotation_quaternion',[1,0,0,0])

        for group in action.groups:
            print (group.name)
            for channel in group.channels:
                data_path = channel.data_path
                if armature.pose and 'pose.bones' in data_path:
                    target_name = data_path.split('"')[1]
                    transform = data_path.split('.')[-1]
                    # print(' * * ' + target_name)
        # for fcurve in action.fcurves:
        #     data_path = fcurve.data_path
        #     target_name = data_path.split('"')[1]
        #     transform = data_path.split('.')[-1]
        #     print('  ' + fcurve.data_path + ' ' + str(fcurve.array_index))
    # pose_bones = set()


def prepare_scene():
    for armature in of_type(bpy.data.objects, 'ARMATURE'):
        prepare_armature(armature)

def get_blend_filename():
    filepath = bpy.data.filepath
    return os.path.splitext(os.path.basename(filepath))[0]

def get_export_filepath():
    name = get_blend_filename()
    script_path = os.path.realpath(__file__)
    return os.path.abspath(os.path.join(os.path.dirname(script_path), '../../', models_path, name, name + '.gltf'))

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
        # print("node :" + obj.name)

def export_gltf():
    filepath = get_export_filepath()
    os.makedirs(os.path.dirname(filepath), exist_ok = True)
    hide_unrenderable()
    bpy.ops.export_scene.gltf(
        filepath = filepath,
        axis_forward = 'Y',
        axis_up = 'Z',
        # value.name = '',
        # value.embed_shaders = False,
        # settings_KHR_technique_webgl.name = '',
        # settings_KHR_technique_webgl.embed_shaders = False,
        draft_prop = False,
        nodes_export_hidden = False,
        nodes_selected_only = False,
        materials_disable = False,
        meshes_apply_modifiers = True,
        meshes_vertex_color_alpha = False,
        meshes_interleave_vertex_data = True,
        animations_object_export = 'ACTIVE',
        animations_armature_export = 'ELIGIBLE',
        animations_shape_key_export = 'ELIGIBLE',
        images_data_storage = 'COPY',
        images_allow_srgb = False,
        buffers_embed_data = False,
        buffers_combine_data = True,
        asset_copyright = '',
        asset_version = '2.0',
        asset_profile = 'WEB',
        gltf_export_binary = False,
        pretty_print = True,
        blocks_prune_unused = True,
        enable_actions = True,
        enable_cameras = False,
        enable_lamps = False,
        enable_materials = True,
        enable_meshes = True,
        enable_textures = True
    )

if __name__ == '__main__':
    prepare_scene()
    export_gltf()
    print('Exported ', get_export_filepath())
