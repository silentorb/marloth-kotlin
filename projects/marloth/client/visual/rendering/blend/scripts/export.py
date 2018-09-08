import bpy
import os
import os.path

models_path = 'src/main/resources/models'

def get_blend_filename():
    filepath = bpy.data.filepath
    return os.path.splitext(os.path.basename(filepath))[0]

def get_export_filepath():
    name = get_blend_filename()
    script_path = os.path.realpath(__file__)
    return os.path.abspath(os.path.join(os.path.dirname(script_path), '../../', models_path, name, name + '.gltf'))

def export_gltf():
    filepath = get_export_filepath()
    os.makedirs(os.path.dirname(filepath), exist_ok = True)
    bpy.ops.export_scene.gltf(
        filepath = filepath,
        axis_forward = 'Y',
        axis_up = 'Z',
        # value.name = '',
        # value.embed_shaders = False,
        # settings_KHR_technique_webgl.name = '',
        # settings_KHR_technique_webgl.embed_shaders = False,
        draft_prop = False,
        nodes_export_hidden = True,
        nodes_selected_only = False,
        materials_disable = False,
        meshes_apply_modifiers = True,
        meshes_vertex_color_alpha = False,
        meshes_interleave_vertex_data = False,
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
    return


if __name__ == '__main__':
    export_gltf()
    print('Exported ', get_export_filepath())
