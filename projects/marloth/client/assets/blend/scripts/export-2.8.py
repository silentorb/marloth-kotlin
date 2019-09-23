import bpy
import os
import os.path
import sys
sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'addon'))
from mythic_lib import get_material_texture_node
sys.path.append(os.path.dirname(__file__))
from baking import bake_all

models_path = 'src/main/resources/models'
textures_path = 'src/main/resources/textures'
script_path = os.path.realpath(__file__)


def get_export_dir(name):
    return os.path.abspath(os.path.join(os.path.dirname(script_path), '../../', models_path, name))


def get_textures_dir():
    return os.path.abspath(os.path.join(os.path.dirname(script_path), '../../', textures_path))


def add_to_map_list(map, key, item):
    if key in map:
        map[key].append(item)
    else:
        map[key] = [item]


def of_type(list, type):
    return [c for c in list if c.type == type]


def delete_texture_nodes():
    for material in bpy.data.materials:
        tree = material.node_tree
        if tree is None:
            return None

        nodes = tree.nodes
        for node in list(nodes):
            if node.bl_idname == 'ShaderNodeTexImage':
                # print("image name " + os.path.basename(node.image.filepath))
                node.image.source = 'GENERATED'
                # nodes.remove(node)


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
    if type == 'box':
        obj['bounds'] = {
            'type': 'box',
            'dimensions': (dimensions.x, dimensions.y, dimensions.z)
        }

def has_dominant_material(obj):
    return any(m.name == obj.name for m in obj.material_slots)


def remove_materials(object, material_slot_indices):
    for i in reversed(material_slot_indices):
        object.active_material_index = i
        bpy.ops.object.material_slot_remove()


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


def render_camera_texture(camera, image_name):
    scene = bpy.context.scene
    scene.camera = camera
    scene.render.filepath = get_textures_dir() + '/' + image_name + scene.render.file_extension
    bpy.ops.render.render(write_still=True)
    print('Exported texture: ' + scene.render.filepath)


def render_camera_textures():
    for camera in bpy.data.cameras:
        obj = bpy.data.objects[camera.name]
        if 'render-texture' in obj:
            render_camera_texture(obj, obj['render-texture'])


def deselect_all():
    bpy.ops.object.select_all(action='DESELECT')


def get_export_objects():
    if 'no-export' in bpy.context.scene:
        return []

    result = []
    for obj in bpy.context.scene.objects:
        if not obj.hide_render and 'no-render' not in obj and 'no-export' not in obj and obj.type in ['MESH', 'LIGHT']:
            print('obj ' + obj.name)
            result.append(obj)
    return result


def set_export_object_visibility(objs):
    for obj in objs:
        if obj.hide_get():
            obj.hide_set(False)
        obj.select_set(True)


def prepare_scene(export_dir):
    # Some of the export operations will fail if the blender file was saved in edit mode.
    # Ensure blender is in object mode and not edit mode
    bpy.ops.object.mode_set(mode='OBJECT')

    deselect_all()
    export_objects = get_export_objects()

    if len(export_objects) > 0:
        os.makedirs(export_dir, exist_ok=True)

    for obj in bpy.context.scene.objects:
        preprocess_bounds_shape(obj)

    render_camera_textures()
    bake_all(export_dir)

    has_objects = len(export_objects) > 0
    if has_objects:
        if 'copy-images' not in bpy.context.scene:
            delete_texture_nodes()

        deselect_all()
        set_export_object_visibility(export_objects)

    return has_objects


def get_blend_filename():
    filepath = bpy.data.filepath
    return os.path.splitext(os.path.basename(filepath))[0]


def get_file_storage_method():
    for image in bpy.data.images:
        if 'resources' in image.filepath:
            return 'REFERENCE'
    return 'COPY'


def export_gltf(filepath):
    bpy.ops.export_scene.gltf(
        export_format='GLTF_SEPARATE',
        ui_tab='GENERAL',
        export_copyright='',
        export_image_format='NAME',
        export_texcoords=True,
        export_normals=True,
        export_draco_mesh_compression_enable=False,
        export_draco_mesh_compression_level=6,
        export_draco_position_quantization=14,
        export_draco_normal_quantization=10,
        export_draco_texcoord_quantization=12,
        export_tangents=False,
        export_materials=True,
        export_colors=False,
        export_cameras=False,
        export_selected=True,
        export_extras=True,
        export_yup=False,
        export_apply=True,
        export_animations=True,
        export_frame_range=True,
        export_frame_step=1,
        export_force_sampling=True,
        export_current_frame=False,
        export_skins=True,
        export_all_influences=False,
        export_morph=True,
        export_morph_normal=True,
        export_morph_tangent=False,
        export_lights=True,
        export_displacement=False,
        will_save_settings=False,
        filepath=filepath
    )


def main():
    name = get_blend_filename()
    export_dir = get_export_dir(name)
    export_file = os.path.join(export_dir, name + '.gltf')
    if prepare_scene(export_dir):
        export_gltf(export_file)
        # bpy.ops.wm.save_as_mainfile(filepath='e:/deleteme.blend')
        print('Exported ', export_file)
    else:
        print('No objects to export')


if __name__ == '__main__':
    main()
