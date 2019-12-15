import bpy
import os
import os.path
import sys
import mathutils
sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'addon'))
from mythic_lib import get_material_texture_node
sys.path.append(os.path.dirname(__file__))
from baking import bake_all, prune_graph_for_texture

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


def prepare_texture_nodes():
    for material in bpy.data.materials:
        # If it's baked then the material is already prepared for image export
        if 'bake' in material:
            continue

        tree = material.node_tree
        if tree is None:
            continue

        nodes = tree.nodes
        texture_nodes = [node for node in list(nodes) if node.bl_idname == 'ShaderNodeTexImage']
        if len(texture_nodes) == 1:
            node = texture_nodes[0]
            image = node.image
            image.source = 'GENERATED'
            prune_graph_for_texture(material, image)
            # for node in texture_nodes:
            #     if node.bl_idname == 'ShaderNodeTexImage':
            #         node.image.source = 'GENERATED'


def get_horizontal_radius(dimensions):
    return max(dimensions.x, dimensions.y) / 2


def is_vector_approximately_non_zero(position, min):
    return abs(position[0]) > min or abs(position[1]) > min or abs(position[2]) > min


def get_shape_offset(bound_box):
    center = sum((mathutils.Vector(b) for b in bound_box), mathutils.Vector()) / 8
    min = 0.03
    if is_vector_approximately_non_zero(center, 0.03):
        return center

    return None


bounds_type_key = 'bounds'


def get_bounds_children(parent):
    return [obj for obj in bpy.data.objects if obj.parent == parent and bounds_type_key in obj]


def aggregate_child_bounds(obj):
    children = [preprocess_bounds_shape(child) for child in get_bounds_children(obj) ]
    print(children)
    return [c for c in children if c != None]


def preprocess_bounds_shape(obj):
    type = str(obj.get(bounds_type_key))
    if type is None:
        return None

    dimensions = obj.dimensions
    # print('shape ' + type)
    bounds = None
    if type == 'composite':
        bounds = {
            'type': 'composite',
            'children': aggregate_child_bounds(obj)
        }
    elif type == 'cylinder':
        bounds = {
            'type': 'cylinder',
            'radius': get_horizontal_radius(dimensions),
            'height': dimensions.z
        }
    elif type == 'mesh':
        bounds = {
            'type': 'mesh',
            'radius': get_horizontal_radius(dimensions),
            'height': dimensions.z
        }
    elif type == 'box':
        bounds = {
            'type': 'box',
            'dimensions': (dimensions.x, dimensions.y, dimensions.z)
        }

    if bounds:
        if type != 'mesh':
            offset = get_shape_offset(obj.bound_box)
            if is_vector_approximately_non_zero(obj.location, 0.01):
                if offset:
                    offset += obj.location
                else:
                    offset = obj.location
            if offset:
                bounds['offset'] = offset
        obj['bounds'] = bounds

    # print('shape end ' + type)

    return bounds


def has_dominant_material(obj):
    return any(m.name == obj.name for m in obj.material_slots)


def remove_materials(object, material_slot_indices):
    for i in reversed(material_slot_indices):
        object.active_material_index = i
        print('removing material for' + object.name)
        bpy.ops.object.material_slot_remove()


def prune_materials():
    for obj in bpy.data.objects:
        if obj.type == 'MESH' and 'no-export' not in obj:
            slots = list(range(1, len(obj.material_slots)))
            remove_materials(obj, slots)


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


# The Blender operator to select/deselect has a bug with needing to get the proper context based on mouse hovering :(
def deselect_all():
    bpy.context.view_layer.objects.active = None
    for obj in bpy.data.objects:
        obj.select_set(False)


'INVOKE_DEFAULT'
def get_export_objects():
    if 'no-export' in bpy.context.scene:
        return []

    result = []
    for obj in bpy.context.scene.objects:
        if not obj.hide_render and 'no-render' not in obj and 'no-export' not in obj and obj.type in ['MESH', 'LIGHT', 'ARMATURE']:
            print(obj.name)
            result.append(obj)
    return result


def set_export_object_visibility(objs):
    for obj in objs:
        if obj.hide_get():
            obj.hide_set(False)
        obj.select_set(True)


def prepare_animations():
    for action in bpy.data.actions:
        markers = []
        for marker in action.pose_markers:
            markers.append({ 'name': marker.name, 'frame': marker.frame })

        if len(markers) > 0:
            action['markers'] = markers


# The GLTF exporter will skip meshes with ngon topology but won't provide any information about the
# mesh that was skipped, such as its name.  Running this function first provides
# a better workflow for handling topology problems
def check_topology(objs):
    errored = False
    for obj in objs:
        if obj.type == 'MESH':
            try:
                obj.data.calc_tangents()
            except:
                errored = True
                print('ERROR: Object ' + obj.name + ' has faces with more than 4 sides')
    # if errored:
    #     raise Exception('Aborted export due to topology issues')

def prepare_scene(export_dir):
    # Some of the export operations will fail if the blender file was saved in edit mode.
    # Ensure blender is in object mode and not edit mode
    # bpy.ops.object.mode_set(mode='OBJECT')

    deselect_all()
    export_objects = get_export_objects()
    check_topology(export_objects)

    if len(export_objects) > 0:
        os.makedirs(export_dir, exist_ok=True)

    for obj in bpy.context.scene.objects:
        preprocess_bounds_shape(obj)

    render_camera_textures()
    # bake_all(export_dir)

    prepare_animations()

    has_objects = len(export_objects) > 0
    if has_objects:
        # prune_materials()
        if 'copy-images' not in bpy.context.scene:
            prepare_texture_nodes()

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
        export_def_bones=True,
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
